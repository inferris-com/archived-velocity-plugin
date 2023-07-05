package com.inferris.player.friends;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inferris.Inferris;
import com.inferris.SerializationModule;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.util.CacheSerializationUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

public class FriendsManager {
    private static FriendsManager instance;
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final Cache<UUID, Friends> caffeineCache;

    private FriendsManager() {
        jedisPool = Inferris.getJedisPool(); // Set Redis server details
        objectMapper = CacheSerializationUtils.createObjectMapper(new SerializationModule());
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
                Friends friends = CacheSerializationUtils.deserializeFriends(json);
                Inferris.getInstance().getLogger().info(json);
                return friends;
            } else {
                jedis.hset("friends", playerUUID.toString(), CacheSerializationUtils.serializeFriends(new Friends()));
                return new Friends();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is a vital method for the caffeine cache. If not updated, and a player is in Redis,
     * then no changes will be made.
     * @param playerUUID
     * @param friends
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

                if(targetPlayer != null){
                    PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(playerUUID);
                    targetPlayer.sendMessage(new TextComponent(ChatColor.GREEN + "You received a friend request from " +
                            playerData.getByBranch().getPrefix(true) + playerData.getRegistry().getUsername()));
                }
            }catch(IllegalArgumentException e){
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);
                if(player != null){
                    player.sendMessage(new TextComponent(ChatColor.RED + e.getMessage()));
                }
                return;
            }
            jedis.hset("friends", playerUUID.toString(), CacheSerializationUtils.serializeFriends(playerFriends));
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
            jedis.hset("friends", playerUUID.toString(), CacheSerializationUtils.serializeFriends(playerFriends));
            jedis.hset("friends", targetUUID.toString(), CacheSerializationUtils.serializeFriends(targetFriends));
            updateCache(playerUUID, playerFriends);
            updateCache(targetUUID, targetFriends);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateRedisData(UUID playerUUID, Friends friends) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("friends", playerUUID.toString(), CacheSerializationUtils.serializeFriends(friends));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Cache<UUID, Friends> getCaffeineCache() {
        return caffeineCache;
    }
}
