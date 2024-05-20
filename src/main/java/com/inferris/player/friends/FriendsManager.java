package com.inferris.player.friends;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inferris.Inferris;
import com.inferris.config.ConfigType;
import com.inferris.serialization.SerializationModule;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.util.SerializationUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class FriendsManager {
    private static FriendsManager instance;
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final Cache<UUID, Friends> caffeineCache;

    private FriendsManager() {
        jedisPool = Inferris.getJedisPool(); // Set Redis server details
        objectMapper = SerializationUtils.createObjectMapper(new SerializationModule());
        caffeineCache = Caffeine.newBuilder().build();
    }

    public static FriendsManager getInstance() {
        if (instance == null) {
            instance = new FriendsManager();
        }
        return instance;
    }

    public Friends getFriendsData(UUID playerUUID) {
        if (caffeineCache.getIfPresent(playerUUID) != null) {
            return caffeineCache.asMap().get(playerUUID);
        } else {
            return getFriendsDataFromRedis(playerUUID);
        }
    }

    public Friends getFriendsDataFromRedis(UUID playerUUID) {
        try (Jedis jedis = jedisPool.getResource()) {
            Inferris.getInstance().getLogger().info("Loading Friends from Redis");
            String json = jedis.hget("friends", playerUUID.toString());

            if (json != null) {
                Friends friends = SerializationUtils.deserializeFriends(json);
                Inferris.getInstance().getLogger().info(json);
                return friends;
            } else {
                jedis.hset("friends", playerUUID.toString(), SerializationUtils.serializeFriends(new Friends()));
                return new Friends();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is a vital method for the caffeine cache. If not updated, and a player is in Redis,
     * then no changes will be made.
     *
     * @param playerUUID Player's UUI D
     * @param friends {@link Friends} class
     */
    public void updateCache(UUID playerUUID, Friends friends) {
        caffeineCache.put(playerUUID, friends);
    }

    public void friendRequest(UUID playerUUID, UUID targetUUID) {
        try (Jedis jedis = jedisPool.getResource()) {
            Friends playerFriends = getFriendsDataFromRedis(playerUUID);
            Friends targetFriends = getFriendsDataFromRedis(targetUUID);
            try {
                playerFriends.addPendingFriend(targetUUID);

                updateCache(playerUUID, playerFriends);
                ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(targetUUID);
                ProxyServer.getInstance().getPlayer(playerUUID).sendMessage(new TextComponent(ChatColor.GREEN + "Sent friend request"));

                if (targetPlayer != null) {
                    PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(playerUUID);
                    targetPlayer.sendMessage(new TextComponent(ChatColor.GREEN + "You received a friend request from " +
                            playerData.getByBranch().getPrefix(true) + playerData.getUsername()));
                }
            } catch (IllegalArgumentException e) {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);
                if (player != null) {
                    player.sendMessage(new TextComponent(ChatColor.RED + e.getMessage()));
                }
                return;
            }
            jedis.hset("friends", playerUUID.toString(), SerializationUtils.serializeFriends(playerFriends));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public void addFriend(UUID playerUUID, UUID targetUUID) {
        try (Jedis jedis = jedisPool.getResource()) {
            Friends playerFriends = getFriendsDataFromRedis(playerUUID);
            Friends targetFriends = getFriendsDataFromRedis(targetUUID);
            playerFriends.acceptFriendRequest(targetUUID);
            targetFriends.acceptFriendRequest(playerUUID);
            jedis.hset("friends", playerUUID.toString(), SerializationUtils.serializeFriends(playerFriends));
            jedis.hset("friends", targetUUID.toString(), SerializationUtils.serializeFriends(targetFriends));
            updateCache(playerUUID, playerFriends);
            updateCache(targetUUID, targetFriends);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFriend(UUID playerUUID, UUID targetUUID) {
        try (Jedis jedis = jedisPool.getResource()) {
            Friends playerFriends = getFriendsDataFromRedis(playerUUID);
            Friends targetFriends = getFriendsDataFromRedis(targetUUID);
            PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(playerUUID);
            PlayerData targetData = PlayerDataManager.getInstance().getRedisDataOrNull(targetUUID);

            try {
                playerFriends.removeFriend(targetUUID);
                targetFriends.removeFriend(playerUUID);
                ProxyServer.getInstance().getPlayer(playerUUID).sendMessage(new TextComponent(ChatColor.GREEN + "You have removed "
                        + targetData.getByBranch().getPrefix(true) + targetData.getUsername() + ChatColor.GREEN + " as a friend"));
            } catch (IllegalArgumentException e) {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);
                if (player != null) {
                    player.sendMessage(new TextComponent(ChatColor.RED + e.getMessage()));
                }
                return;
            }

            jedis.hset("friends", playerUUID.toString(), SerializationUtils.serializeFriends(playerFriends));
            jedis.hset("friends", targetUUID.toString(), SerializationUtils.serializeFriends(targetFriends));

            updateCache(playerUUID, playerFriends);
            updateCache(targetUUID, targetFriends);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void rejectFriendRequest(UUID playerUUID, UUID targetUUID) {
        try (Jedis jedis = jedisPool.getResource()) {
            Friends playerFriends = getFriendsDataFromRedis(playerUUID);
            Friends targetFriends = getFriendsDataFromRedis(targetUUID);
            PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(playerUUID);
            PlayerData targetData = PlayerDataManager.getInstance().getRedisDataOrNull(targetUUID);

            try {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);

                targetFriends.removePendingFriendRequest(playerUUID);
                player.sendMessage(new TextComponent(ChatColor.GREEN + "Rejected " + targetData.getUsername() + "'s friend request"));
                jedis.hset("friends", targetUUID.toString(), SerializationUtils.serializeFriends(targetFriends));
                updateCache(targetUUID, targetFriends);

            } catch (IllegalArgumentException e) {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);
                player.sendMessage(new TextComponent(ChatColor.RED + e.getMessage()));

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void listFriends(UUID playerUUID, int pageNumber) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);
        Friends friends = caffeineCache.getIfPresent(playerUUID);
        List<String> onlineFriends = new ArrayList<>();
        List<String> offlineFriends = new ArrayList<>();

        List<UUID> sortedList = new ArrayList<>(friends.getFriendsList());

        // Fetch PlayerData objects for all friends
        List<PlayerData> playerDataList = new ArrayList<>();

        for (UUID friendUUID : sortedList) {
            PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(friendUUID);
            if (playerData != null) {
                playerDataList.add(playerData);
            }
        }

        int pageSize = Inferris.getInstance().getConfigurationHandler().getConfig(ConfigType.CONFIG).getSection("friends").getInt("page-size");
        player.sendMessage(new TextComponent(String.valueOf(pageSize)));
        int startIndex = (pageNumber - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, playerDataList.size());

        String separator = ChatColor.DARK_GRAY + " -";
        String online = ChatColor.GREEN + " online";
        String offline = ChatColor.RED + " offline";

        // Custom comparator to prioritize online players first, then sort alphabetically
        Comparator<PlayerData> playerComparator = Comparator.comparing((PlayerData playerData) -> {

            if (ProxyServer.getInstance().getPlayer(playerData.getUuid()) != null || playerData.getVanishState() == VanishState.DISABLED) {
                return 0; // Online players
            } else {
                return 1; // Offline players
            }
        }).thenComparing(playerData -> playerData.getUsername());

        // Sort the playerDataList based on the custom comparator
        playerDataList.sort(playerComparator);

        for (int i = startIndex; i < endIndex; i++) {
            PlayerData playerData = playerDataList.get(i);
            UUID friendUUID = playerData.getUuid();
            ProxiedPlayer friendPlayer = ProxyServer.getInstance().getPlayer(friendUUID);

            String playerName = playerData.getUsername();
            String prefix = playerData.getByBranch().getPrefix(true);
            String playerStr = ChatColor.YELLOW + "Player ";
            String is = ChatColor.YELLOW + " is";

            if (friendPlayer != null && playerData.getVanishState() == VanishState.DISABLED) {
                onlineFriends.add(playerStr + prefix + ChatColor.RESET + playerName + is + online);
            } else {
                offlineFriends.add(playerStr + prefix + ChatColor.RESET + playerName + is + offline);
            }
        }

        // Sort onlineFriends alphabetically
        onlineFriends.sort(Comparator.comparing(friend -> ChatColor.stripColor(friend).substring(8)));

        // Display the friends list
        player.sendMessage(new TextComponent(ChatColor.YELLOW + "          Friends List"));
        player.sendMessage(new TextComponent(ChatColor.GRAY + "•——————•°•✿•°•——————•"));

        // Send onlineFriends first
        onlineFriends.forEach(message -> player.sendMessage(TextComponent.fromLegacyText(message)));

        // Add a separator between onlineFriends and offlineFriends
        if (!onlineFriends.isEmpty() && !offlineFriends.isEmpty()) {
            player.sendMessage(new TextComponent(separator));
        }

        // Send offlineFriends next
        offlineFriends.forEach(message -> player.sendMessage(TextComponent.fromLegacyText(message)));
    }

    public void updateRedisData(UUID playerUUID, Friends friends) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("friends", playerUUID.toString(), SerializationUtils.serializeFriends(friends));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Cache<UUID, Friends> getCaffeineCache() {
        return caffeineCache;
    }
}
