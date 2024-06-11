package com.inferris.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.inferris.Inferris;
import com.inferris.events.redis.EventPayload;
import com.inferris.events.redis.PlayerAction;
import com.inferris.player.friends.Friends;
import com.inferris.player.friends.FriendsManager;
import com.inferris.serialization.SerializationModule;
import com.inferris.database.DatabasePool;
import com.inferris.database.Table;
import com.inferris.player.coins.Coins;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RanksManager;
import com.inferris.server.Server;
import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;
import com.inferris.server.jedis.JedisChannel;
import com.inferris.util.SerializationUtils;
import com.inferris.util.DatabaseUtils;
import com.inferris.util.ServerUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The PlayerDataManager class manages the retrieval, caching, and updating of player data from Redis server and in-memory cache.
 * It provides methods to retrieve player data, update player data in Redis, invalidate cache entries, and perform other related operations.
 * <p>
 * PlayerDataManager utilizes a combination of Redis server and an in-memory cache (Caffeine) to store and retrieve player data.
 * Redis is used as a persistent storage for player data, while the in-memory cache provides faster access to frequently accessed data.
 * <p>
 * This class follows the singleton design pattern to ensure a single instance of PlayerDataManager is used throughout the application.
 * The instance can be obtained using the {@link #getInstance()} method.
 * <p>
 * To retrieve player data, use the {@link #getPlayerData(ProxiedPlayer)} method, which first checks if the data is available in the cache,
 * and if not, retrieves it from the Redis server. If no data is found, an empty {@link PlayerData} object is created.
 * <p>
 * The class also provides methods to update player data in the Redis server using {@link #updateRedisData(ProxiedPlayer, PlayerData)},
 * retrieve player data from Redis without creating an empty object if no data is found using {@link #getRedisDataOrNull(ProxiedPlayer)},
 * and perform other operations like checking if a player has joined before, invalidating Redis entries and cache, etc.
 *
 * @since 1.0
 */
public class PlayerDataManager {

    private static PlayerDataManager instance;
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final Cache<UUID, PlayerData> caffeineCache;
    private final Logger logger = Inferris.getInstance().getLogger();
    private final FetchPlayer fetchPlayer;

    private PlayerDataManager() {
        jedisPool = Inferris.getJedisPool(); // Set Redis server details
        objectMapper = SerializationUtils.createObjectMapper(new SerializationModule());
        caffeineCache = Caffeine.newBuilder().build();
        fetchPlayer = new FetchPlayer(ServiceLocator.getPlayerDataService());
    }

    public static PlayerDataManager getInstance() {
        if (instance == null) {
            instance = new PlayerDataManager();
        }
        return instance;
    }


    /**
     * Retrieves the player data for the specified player. The method first checks if the data is available in the
     * in-memory cache, and if not, retrieves it from the Redis server. If no data is found, an empty PlayerData object
     * is created.
     *
     * @param player The ProxiedPlayer object representing the player.
     * @return The PlayerData object containing the player's data.
     */

    public PlayerData getPlayerData(ProxiedPlayer player) {
        if (caffeineCache.getIfPresent(player.getUniqueId()) != null) {
            return caffeineCache.asMap().get(player.getUniqueId());
        } else {
            Inferris.getInstance().getLogger().severe("getPlayerData failed, trying getRedisData");
            return getRedisData(player);
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        PlayerData cachedData = caffeineCache.getIfPresent(uuid);

        if (cachedData != null) {
            return cachedData;
        } else {
            return getRedisData(uuid);
        }
    }


    // TODO DEBUG, REMOVE
    public PlayerData getPlayerData(ProxiedPlayer player, String debugOnly) {
        if (caffeineCache.getIfPresent(player.getUniqueId()) != null) {
            return caffeineCache.asMap().get(player.getUniqueId());
        } else {
            Inferris.getInstance().getLogger().severe("Trying getRedisData over at " + debugOnly);
            return getRedisData(player);
        }
    }

    public CompletableFuture<PlayerData> getPlayerDataAsync(ProxiedPlayer player) {
        UUID playerUUID = player.getUniqueId();

        // Check the caffeine cache first
        PlayerData cachedData = caffeineCache.getIfPresent(playerUUID);
        if (cachedData != null) {
            return CompletableFuture.completedFuture(cachedData);
        }

        // If not found in cache, fetch from Redis asynchronously
        return CompletableFuture.supplyAsync(() -> {
            Inferris.getInstance().getLogger().severe("Fetching data from Redis for: " + player.getName());
            return getPlayerData(player);
        }, Inferris.getInstance().getExecutorService());  // Assuming you have an Executor for async tasks
    }

    public CompletableFuture<PlayerData> getPlayerDataAsync(UUID uuid) {

        // Check the caffeine cache first
        PlayerData cachedData = caffeineCache.getIfPresent(uuid);
        if (cachedData != null) {
            return CompletableFuture.completedFuture(cachedData);
        }

        // If not found in cache, fetch from Redis asynchronously
        return CompletableFuture.supplyAsync(() -> {
            // Get the current stack trace
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            // Log the details of the calling method (the element at index 2 should be the caller)
            if (stackTrace.length > 2) {
                StackTraceElement caller = stackTrace[2];
                String logMessage = String.format("Method called from: %s.%s(%s:%d)",
                        caller.getClassName(), caller.getMethodName(), caller.getFileName(), caller.getLineNumber());
                Inferris.getInstance().getLogger().log(Level.SEVERE, logMessage);
            }
            return getPlayerData(uuid);
        }, Inferris.getInstance().getExecutorService());  // Assuming you have an Executor for async tasks
    }

    /**
     * Retrieves the player data from the Redis server for the specified player.
     *
     * @param player The ProxiedPlayer object representing the player.
     * @return The PlayerData object containing the player's data, or an empty PlayerData object if no data is found.
     */

    public PlayerData getRedisData(ProxiedPlayer player) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.hget("playerdata", player.getUniqueId().toString());
            if (json != null) {
                return SerializationUtils.deserializePlayerData(json);
            } else {
                Inferris.getInstance().getLogger().severe("Trying getPlayerDataFromDatabase");
                return fetchPlayer.getPlayerDataFromDatabase(player.getUniqueId());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public PlayerData getRedisData(UUID uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.hget("playerdata", uuid.toString());
            if (json != null) {
                return SerializationUtils.deserializePlayerData(json);
            } else {
                Inferris.getInstance().getLogger().severe("Trying getPlayerDataFromDatabase");
                return fetchPlayer.getPlayerDataFromDatabase(uuid);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Retrieves the player data from the Redis server for the specified player, or returns null if no data is found.
     *
     * @param player The ProxiedPlayer object representing the player.
     * @return The PlayerData object containing the player's data, or null if no data is found.
     */

    public PlayerData getRedisDataOrNull(ProxiedPlayer player) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.hget("playerdata", player.getUniqueId().toString());
            if (json != null) {
                return SerializationUtils.deserializePlayerData(json);
            } else {
                return null;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the Redis data associated with the given player.
     * If no data is found, it returns null.
     *
     * @param uuid The unique identifier
     * @return Either the {@link PlayerData} object, or null, if no data is found
     * @since 1.0
     */

    public PlayerData getRedisDataOrNull(UUID uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.hget("playerdata", uuid.toString());
            if (json != null) {
                return SerializationUtils.deserializePlayerData(json);
            } else {
                return null;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Updates the player data for the specified player in the Redis server.
     *
     * @param player     The ProxiedPlayer object representing the player.
     * @param playerData The PlayerData object containing the updated player data.
     */

    public void updateAllData(ProxiedPlayer player, PlayerData playerData) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("playerdata", player.getUniqueId().toString(), SerializationUtils.serializePlayerData(playerData));
            updateCaffeineCache(player, playerData);
            Inferris.getInstance().getLogger().info("Updated all data and Redis information via Jedis. Caches updated!");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateAllData(UUID player, PlayerData playerData) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("playerdata", player.toString(), SerializationUtils.serializePlayerData(playerData));
            updateCaffeineCache(player, playerData);
            Inferris.getInstance().getLogger().info("Updated all data and Redis information via Jedis. Caches updated!");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateAllDataAndPush(ProxiedPlayer player, PlayerData playerData) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("playerdata", player.getUniqueId().toString(), SerializationUtils.serializePlayerData(playerData));
            updateCaffeineCache(player, playerData);
            Inferris.getInstance().getLogger().info("Updated all data and Redis information via Jedis. We let the front-end know, it has the cue!");

            //jedis.publish(JedisChannels.PLAYERDATA_UPDATE.getChannelName(), player.getUniqueId().toString());
            jedis.publish(JedisChannel.PLAYERDATA_UPDATE.getChannelName(), new EventPayload(player.getUniqueId(),
                    PlayerAction.UPDATE_PLAYER_DATA,
                    null,
                    Inferris.getInstanceId()).toPayloadString());
            Inferris.getInstance().getLogger().severe(player.getUniqueId().toString());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateAllDataAndPush(UUID uuid, PlayerData playerData) {
        try (Jedis jedis = jedisPool.getResource()) {
            // Serialize and save player data to Redis
            jedis.hset("playerdata", uuid.toString(), SerializationUtils.serializePlayerData(playerData));

            // Retrieve the ProxiedPlayer object
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

            // Check if the player is not null and is connected
            if (player != null && player.isConnected()) {
                updateCaffeineCache(player, playerData);
                Inferris.getInstance().getLogger().info("Updated Caffeine cache for online player: " + player.getName());
            } else {
                // Log detailed information if the player is null or not connected
                if (player == null) {
                    Inferris.getInstance().getLogger().severe("ProxiedPlayer is null for UUID: " + uuid);
                } else {
                    Inferris.getInstance().getLogger().severe("ProxiedPlayer is not connected for UUID: " + uuid);
                }
            }

            // Log update to Redis and publish the update
            Inferris.getInstance().getLogger().info("Updated all data and Redis information via Jedis. We let the front-end know, it has the cue!");
            //jedis.publish(JedisChannels.PLAYERDATA_UPDATE.getChannelName(), uuid.toString());
            jedis.publish(JedisChannel.PLAYERDATA_UPDATE.getChannelName(), new EventPayload(player.getUniqueId(),
                    PlayerAction.UPDATE_PLAYER_DATA,
                    null,
                    Inferris.getInstanceId()).toPayloadString());
            Inferris.getInstance().getLogger().severe("Published update for UUID: " + uuid);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateAllDataAndPush(ProxiedPlayer player, PlayerData playerData, JedisChannel jedisChannels) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("playerdata", player.getUniqueId().toString(), SerializationUtils.serializePlayerData(playerData));
            updateCaffeineCache(player, playerData);
            Inferris.getInstance().getLogger().info("Updated all data and Redis information via Jedis. We let the front-end know, it has the cue!");

            switch (jedisChannels) {
                case PLAYERDATA_UPDATE:
                case PLAYERDATA_VANISH:
                    jedis.publish(jedisChannels.getChannelName(), new EventPayload(player.getUniqueId(),
                                    PlayerAction.UPDATE_PLAYER_DATA,
                                    null,
                                    Inferris.getInstanceId()).toPayloadString());

                    Inferris.getInstance().getLogger().severe("Instance ID: " + Inferris.getInstanceId());
                    //case PLAYERDATA_VANISH:jedis.publish(jedisChannels.getChannelName(), CacheSerializationUtils.serializePlayerData(playerData));

            }
            Inferris.getInstance().getLogger().severe(player.getUniqueId().toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updatePlayerDataTable(PlayerData playerData) {
        String[] columnNames = {"username", "coins", "channel", "vanished"};
        boolean isVanished = playerData.getVanishState() == VanishState.ENABLED;
        Object[] values = {playerData.getUsername(), playerData.getCoins(), playerData.getChannel().name(), isVanished};
        String whereClause = "uuid = ?";
        try (Connection connection = DatabasePool.getConnection()) {
            DatabaseUtils.updateData(connection, "player_data", columnNames, values, whereClause);
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().warning("Failed to update player_data in the database: " + e.getMessage());
        }
    }
    public void updateRankTable(Rank rank, UUID uuid) {
        String[] columnNames = {"staff", "builder", "donor", "other"};
        Object[] values = {rank.getStaff(), rank.getBuilder(), rank.getDonor(), rank.getOther()};
        String whereClause = "uuid = ?";
        try (Connection connection = DatabasePool.getConnection()) {
            DatabaseUtils.updateData(connection, "rank", columnNames, values, whereClause);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update rank in the database", e);
        }
    }

    public void updateProfileTable(Profile profile, UUID uuid) {
        String[] columnNames = {"join_date", "bio", "pronouns", "xenforo_id", "discord_linked", "is_flagged"};
        Object[] values = {profile.getRegistrationDate(), profile.getBio(), profile.getPronouns(), profile.getXenforoId(), profile.isDiscordLinked(), profile.isFlagged()};
        String whereClause = "uuid = ?";
        try (Connection connection = DatabasePool.getConnection()) {
            DatabaseUtils.updateData(connection, "profile", columnNames, values, whereClause);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update profile in the database", e);
        }
    }

    public void updateRedisData(ProxiedPlayer player, PlayerData playerData) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("playerdata", player.getUniqueId().toString(), SerializationUtils.serializePlayerData(playerData));
            Inferris.getInstance().getLogger().info("Updated Redis information via Jedis. Caches updated!");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCaffeineCache(ProxiedPlayer player, PlayerData playerData) {
        caffeineCache.put(player.getUniqueId(), playerData);
        Inferris.getInstance().getLogger().info("Updated Caffeine cache for player: " + player.getName());
    }

    public void updateCaffeineCache(UUID uuid, PlayerData playerData) {
        caffeineCache.put(uuid, playerData);
        Inferris.getInstance().getLogger().info("Updated Caffeine cache for player: " + getPlayerData(uuid).getUsername());
    }

    public PlayerData getDeserializedRedisData(ProxiedPlayer player) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(player.getUniqueId().toString());
            if (json != null) {
                return SerializationUtils.deserializePlayerData(json);
            } else {
                return createEmpty(player); // Create an empty Registry object instead of returning null
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    // Insert a new player into the database - default values
    public void insertPlayerDataToDatabase(Connection connection, UUID uuid, String username) {
        try (PreparedStatement insertPlayersStatement = connection.prepareStatement("INSERT INTO " + Table.PLAYER_DATA.getName() + " (uuid, username, coins, channel, vanished) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement insertProfileStatement = connection.prepareStatement("INSERT INTO " + Table.PROFILE.getName() + " (uuid, join_date, bio, pronouns) VALUES (?, ?, ?, ?)")) {

            insertPlayersStatement.setString(1, uuid.toString());
            insertPlayersStatement.setString(2, username);
            insertPlayersStatement.setInt(3, PlayerDefault.COIN_BALANCE.getValue());
            insertPlayersStatement.setString(4, String.valueOf(Channel.NONE));
            insertPlayersStatement.setInt(5, 0);
            insertPlayersStatement.execute();

            insertProfileStatement.setString(1, uuid.toString());
            insertProfileStatement.setString(2, String.valueOf(Instant.now().getEpochSecond()));
            insertProfileStatement.setString(3, null);
            insertProfileStatement.setString(4, null);
            insertProfileStatement.execute();

            //RanksManager.getInstance().loadRanks(uuid, connection); todo remove
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().warning(e.getMessage());
        }
    }

    /**
     * Inserts the player data into the database if it does not already exist.
     *
     * @param uuid     The UUID of the player.
     * @param username The username of the player.
     * @return true if the player data was inserted, false if it already exists.
     */
    public boolean insertPlayerDataIfNotExists(UUID uuid, String username) {
        boolean inserted = false;
        try (Connection connection = DatabasePool.getConnection()) {
            // Check if the player data exists in the database
            PreparedStatement queryStatement = connection.prepareStatement("SELECT COUNT(*) FROM " + Table.PLAYER_DATA.getName() + " WHERE uuid = ?");
            queryStatement.setString(1, uuid.toString());
            ResultSet resultSet = queryStatement.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) == 0) {
                // Insert the player data if it doesn't exist
                insertPlayerDataToDatabase(connection, uuid, username);
                inserted = true;
            }

            // Retrieve the player data
            PlayerData playerData = fetchPlayer.getPlayerDataFromDatabase(uuid, username, false);

            // Cache the player data and update Redis
            String playerDataJson = SerializationUtils.serializePlayerData(playerData);
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.hset("playerdata", uuid.toString(), playerDataJson);
                updateCaffeineCache(uuid, playerData);
            }
        } catch (SQLException | JsonProcessingException e) {
            Inferris.getInstance().getLogger().warning(e.getMessage());
        }
        return inserted;
    }


    public void deletePlayerData(PlayerData playerData) {
        UUID playerUUID = playerData.getUuid();

        // Step 1: Fetch the list of friends
        FriendsManager friendsManager = FriendsManager.getInstance();
        Friends playerFriends = friendsManager.getFriendsData(playerUUID);
        List<UUID> friendsList = new ArrayList<>(playerFriends.getFriendsList());

        // Step 2: Update each friend's data
        for (UUID friendUUID : friendsList) {
            Friends friendData = friendsManager.getFriendsData(friendUUID);
            friendData.removeFriend(playerUUID);
            friendsManager.updateCache(friendUUID, friendData);
            friendsManager.updateRedisData(friendUUID, friendData);
        }

        // Step 3: Remove the player's data from all necessary tables
        try (Connection connection = DatabasePool.getConnection()) {
            DatabaseUtils.removeData(connection, Table.PLAYER_DATA.getName(), "`uuid` = '" + playerData.getUuid().toString() + "'");
            DatabaseUtils.removeData(connection, "`rank`", "`uuid` = '" + playerData.getUuid().toString() + "'");
            DatabaseUtils.removeData(connection, Table.PROFILE.getName(), "`uuid` = '" + playerData.getUuid().toString() + "'");
            DatabaseUtils.removeData(connection, Table.VERIFICATION.getName(), "`uuid` = '" + playerData.getUuid().toString() + "'");
            DatabaseUtils.removeData(connection, Table.VERIFICATION_SESSIONS.getName(), "`uuid` = '" + playerData.getUuid().toString() + "'");
        } catch (SQLException e) {
            logger.severe(e.getMessage());
        }

        // Step 4: Remove the player's data from Redis and cache
        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.hdel("playerdata", playerData.getUuid().toString());
            jedis.hdel("friends", playerData.getUuid().toString());
            friendsManager.getCaffeineCache().invalidate(playerUUID);
        }
    }


    /**
     * Logs relevant information about the player's data.
     *
     * @param playerData The PlayerData object to log.
     */
    public void logPlayerData(PlayerData playerData) {
        if (ServerStateManager.getCurrentState() == ServerState.DEBUG || ServerStateManager.getCurrentState() == ServerState.DEV) {
            Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getRank().getBranchID(Branch.STAFF)));
            Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getVanishState()));
            Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getChannel()));
            Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getCoins()));
        }
    }

    private PlayerData createEmpty(ProxiedPlayer player) {
        // Create and return an empty Registry object with default values
        return new PlayerData(player.getUniqueId(), player.getName(),
                new Rank(0, 0, 0, 0),
                new Profile(null, null, null, 0, false, false),
                36, Channel.NONE, VanishState.DISABLED, Server.LOBBY);
    }

    private PlayerData createEmpty(UUID uuid, String username) {
        // Create and return an empty Registry object with default values
        return new PlayerData(uuid, username,
                new Rank(0, 0, 0, 0),
                new Profile(null, null, null, 0, false, false),
                36, Channel.NONE, VanishState.DISABLED, Server.LOBBY);
    }

    public void invalidateRedisEntry(ProxiedPlayer player) {
        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.del("playerdata", player.getUniqueId().toString());
        }
    }

    public void invalidateCache(ProxiedPlayer player) {
        caffeineCache.invalidate(player.getUniqueId());
    }

    public Cache<UUID, PlayerData> getCache() {
        return caffeineCache;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}