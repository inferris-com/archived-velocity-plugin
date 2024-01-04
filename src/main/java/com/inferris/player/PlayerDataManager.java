package com.inferris.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.inferris.Inferris;
import com.inferris.SerializationModule;
import com.inferris.database.DatabasePool;
import com.inferris.database.Tables;
import com.inferris.player.coins.Coins;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RanksManager;
import com.inferris.server.Server;
import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;
import com.inferris.server.jedis.JedisChannels;
import com.inferris.util.CacheSerializationUtils;
import com.inferris.util.DatabaseUtils;
import com.inferris.util.ServerUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
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

    private PlayerDataManager() {
        jedisPool = Inferris.getJedisPool(); // Set Redis server details
        objectMapper = CacheSerializationUtils.createObjectMapper(new SerializationModule());
        caffeineCache = Caffeine.newBuilder().build();
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
            Inferris.getInstance().getLogger().severe("Trying getRedisData");
            return getRedisData(player);
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        if (caffeineCache.getIfPresent(uuid) != null) {
            return caffeineCache.asMap().get(uuid);
        } else {
            return getRedisData(uuid);
        }
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
                return CacheSerializationUtils.deserializePlayerData(json);
            } else {
                return this.getPlayerDataFromDatabase(player.getUniqueId());
                //return createEmpty(player); // Create an empty Registry object instead of returning null
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public PlayerData getRedisData(UUID uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.hget("playerdata", uuid.toString());
            if (json != null) {
                return CacheSerializationUtils.deserializePlayerData(json);
            } else {
                Inferris.getInstance().getLogger().severe("Trying getPlayerDataFromDatabase");
                return this.getPlayerDataFromDatabase(uuid);
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
                return CacheSerializationUtils.deserializePlayerData(json);
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
                return CacheSerializationUtils.deserializePlayerData(json);
            } else {
                return null;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the UUID associated with a given username from player data stored in Redis.
     *
     * @param username The username to search for.
     * @return The UUID corresponding to the provided username, or null if no match is found.
     */
    public UUID getUUIDByUsername(String username) {
        UUID uuid;
        PlayerData playerData = null;
        
        try (Jedis jedis = jedisPool.getResource()) {
            String playerDataJson = jedis.hget("playerdata", username);

            if (playerDataJson != null) {
                JsonElement jsonElement = new Gson().fromJson(playerDataJson, JsonElement.class);
                String uuidStr = jsonElement.getAsJsonObject().get("uuid").getAsString();
                return UUID.fromString(uuidStr);
            } else {
                // Not found in Jedis, retrieve from the database
                String condition = "`username` = '" + username + "'";
                try(Connection connection = DatabasePool.getConnection()){
                    ResultSet resultSet = DatabaseUtils.queryData(connection, Tables.PLAYER_DATA.getName(), new String[]{"uuid"}, condition);
                    if(resultSet.next()) {
                        uuid = UUID.fromString(resultSet.getString("uuid"));
                        playerData = getPlayerDataFromDatabase(uuid);
                    }
                }catch(SQLException e){
                    Inferris.getInstance().getLogger().severe(e.getMessage());
                }
                if (playerData != null) {
                    return playerData.getUuid();
                }
            }
            return null; // Not found in both Jedis and the database
        }
    }

    public boolean hasUUIDByUsername(String username) {
        UUID uuid = getUUIDByUsername(username);
        return uuid != null;
    }

    /**
     * Updates the player data for the specified player in the Redis server.
     *
     * @param player     The ProxiedPlayer object representing the player.
     * @param playerData The PlayerData object containing the updated player data.
     */

    public void updateAllData(ProxiedPlayer player, PlayerData playerData) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("playerdata", player.getUniqueId().toString(), CacheSerializationUtils.serializePlayerData(playerData));
            updateCaffeineCache(player, playerData);
            Inferris.getInstance().getLogger().info("Updated all data and Redis information via Jedis. Caches updated!");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateAllDataAndPush(ProxiedPlayer player, PlayerData playerData) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("playerdata", player.getUniqueId().toString(), CacheSerializationUtils.serializePlayerData(playerData));
            updateCaffeineCache(player, playerData);
            Inferris.getInstance().getLogger().info("Updated all data and Redis information via Jedis. Caches updated!");

            jedis.publish(JedisChannels.PROXY_TO_SPIGOT_PLAYERDATA_CACHE_UPDATE.getChannelName(), CacheSerializationUtils.serializePlayerData(playerData));

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateRedisData(ProxiedPlayer player, PlayerData playerData) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset("playerdata", player.getUniqueId().toString(), CacheSerializationUtils.serializePlayerData(playerData));
            Inferris.getInstance().getLogger().info("Updated Redis information via Jedis. Caches updated!");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCaffeineCache(ProxiedPlayer player, PlayerData playerData) {
        caffeineCache.put(player.getUniqueId(), playerData);
        Inferris.getInstance().getLogger().info("Updated Caffeine cache for player: " + player.getName());
    }

    public PlayerData getDeserializedRedisData(ProxiedPlayer player) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(player.getUniqueId().toString());
            if (json != null) {
                return CacheSerializationUtils.deserializePlayerData(json);
            } else {
                return createEmpty(player); // Create an empty Registry object instead of returning null
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public PlayerData getPlayerDataFromDatabase(UUID uuid) {
        PlayerData playerData = null;
        Profile profile = null;
        String username = null;

        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM " + Tables.PLAYER_DATA.getName() + " WHERE uuid = ?")) {

            queryStatement.setString(1, uuid.toString());
            ResultSet resultSet = queryStatement.executeQuery();

            if (resultSet.next()) {
                username = resultSet.getString("username");
                int coins = resultSet.getInt("coins");
                Channels channel = Channels.valueOf(resultSet.getString("channel"));
                int vanished = resultSet.getInt("vanished");

                VanishState vanishState = VanishState.DISABLED;

                Rank rank = RanksManager.getInstance().loadRanks(uuid, connection);
//                if (vanished == 1 || (rank != null && rank.getBranchID(Branch.STAFF) >= 3)) {
//                    vanishState = VanishState.ENABLED;
//                }

                if (ProxyServer.getInstance().getPlayer(uuid) != null) {
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                    if (!username.equals(player.getName())) {
                        username = player.getName();
                        playerData = this.getPlayerData(player);
                        playerData.setUsername(player.getName());
                        updateAllData(player, playerData);
                    }
                }

                // Query for profile data
                LocalDate registrationDate = null;
                String bio = null;
                String pronouns = null;
                int xenforoId = 0;

                PreparedStatement selectProfileStatement = connection.prepareStatement("SELECT * FROM " + Tables.PROFILE.getName() + " WHERE uuid = ?");
                selectProfileStatement.setString(1, uuid.toString());
                ResultSet profileResultSet = selectProfileStatement.executeQuery();

                if (profileResultSet.next()) {
                    registrationDate = LocalDate.parse(profileResultSet.getString("join_date"));
                    bio = profileResultSet.getString("bio");
                    pronouns = profileResultSet.getString("pronouns");
                    xenforoId = profileResultSet.getInt("xenforo_id"); // Replace with the actual column name
                    profile = new Profile(registrationDate, bio, pronouns, xenforoId);
                }

                playerData = new PlayerData(uuid, username, rank, profile, new Coins(coins), channel, vanishState, Server.LOBBY);
            }
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().warning(e.getMessage());
        }

        return playerData;
    }

    // PlayerData. It returns null if not in database
    // PlayerData playerData = getPlayerDataFromDatabase(player.getUniqueId(), player.getName(), true);
    public PlayerData getPlayerDataFromDatabase(UUID uuid, String username, boolean insertData) {
        PlayerData playerData = null;
        Profile profile = null;

        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM " + Tables.PLAYER_DATA.getName() + " WHERE uuid = ?")) {

            queryStatement.setString(1, uuid.toString());
            ResultSet resultSet = queryStatement.executeQuery();

            if (resultSet.next()) {
                username = resultSet.getString("username");
                int coins = resultSet.getInt("coins");
                Channels channel = Channels.valueOf(resultSet.getString("channel"));
                int vanished = resultSet.getInt("vanished");

                VanishState vanishState = VanishState.DISABLED;

                Rank rank = RanksManager.getInstance().loadRanks(uuid, connection);
//                if (vanished == 1 || (rank != null && rank.getBranchID(Branch.STAFF) >= 3)) {
//                    vanishState = VanishState.ENABLED;
//                }

                if (ProxyServer.getInstance().getPlayer(uuid) != null) {
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                    if (!username.equals(player.getName())) {
                        username = player.getName();
                        playerData = this.getPlayerData(player);
                        playerData.setUsername(player.getName());
                        updateAllData(player, playerData);
                    }
                }

                // Query for profile data
                LocalDate registrationDate = null;
                String bio = null;
                String pronouns = null;
                int xenforoId = 0;

                PreparedStatement selectProfileStatement = connection.prepareStatement("SELECT * FROM " + Tables.PROFILE.getName() + " WHERE uuid = ?");
                selectProfileStatement.setString(1, uuid.toString());
                ResultSet profileResultSet = selectProfileStatement.executeQuery();

                if (profileResultSet.next()) {
                    registrationDate = LocalDate.parse(profileResultSet.getString("join_date"));
                    bio = profileResultSet.getString("bio");
                    pronouns = profileResultSet.getString("pronouns");
                    xenforoId = profileResultSet.getInt("xenforo_id"); // Replace with the actual column name
                    profile = new Profile(registrationDate, bio, pronouns, xenforoId);
                }

                playerData = new PlayerData(uuid, username, rank, profile, new Coins(coins), channel, vanishState, Server.LOBBY);
            } else {
                if (insertData) {
                    this.insertPlayerDataToDatabase(connection, uuid, username);
                }
            }
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().warning(e.getMessage());
        }
        return playerData;
    }

    public void insertPlayerDataToDatabase(Connection connection, UUID uuid, String username) {
        try (PreparedStatement insertPlayersStatement = connection.prepareStatement("INSERT INTO " + Tables.PLAYER_DATA.getName() + " (uuid, username, coins, channel, vanished) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement insertProfileStatement = connection.prepareStatement("INSERT INTO " + Tables.PROFILE.getName() + " (uuid, join_date, bio, pronouns) VALUES (?, ?, ?, ?)")) {

            insertPlayersStatement.setString(1, uuid.toString());
            insertPlayersStatement.setString(2, username);
            insertPlayersStatement.setInt(3, PlayerDefaults.COIN_BALANCE.getValue());
            insertPlayersStatement.setString(4, String.valueOf(Channels.NONE));
            insertPlayersStatement.setInt(5, 0);
            insertPlayersStatement.execute();

            insertProfileStatement.setString(1, uuid.toString());
            insertProfileStatement.setString(2, String.valueOf(LocalDate.now()));
            insertProfileStatement.setString(3, null);
            insertProfileStatement.setString(4, null);
            insertProfileStatement.execute();
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().warning(e.getMessage());
        }
    }


    /**
     * Checks if the provided player has joined before by looking up their data in Redis and caching within Caffeine if necessary.
     *
     * @param player The ProxiedPlayer object representing the player to check.
     */
    public void checkJoinedBefore(ProxiedPlayer player) {
        boolean isDebug = ServerStateManager.getCurrentState() == ServerState.DEBUG;

        ServerUtil.log("Checking Jedis #checkJoinedBefore", Level.WARNING, ServerState.DEBUG);

        try (Jedis jedis = getJedisPool().getResource()) {
            UUID playerUUID = player.getUniqueId();

            if (jedis.hexists("playerdata", playerUUID.toString())) {
                ServerUtil.log("Exists in Jedis", Level.WARNING, ServerState.DEBUG);

                if (caffeineCache.getIfPresent(playerUUID) == null) {
                    String playerDataJson = jedis.hget("playerdata", playerUUID.toString());
                    PlayerData playerData = CacheSerializationUtils.deserializePlayerData(playerDataJson);
                    updateCaffeineCache(player, playerData);
                    logPlayerData(playerData);

                    // Published in join event
                }
            } else {
                ServerUtil.log("Not in Redis, caching", Level.WARNING, ServerState.DEBUG);
                getPlayerDataFromDatabase(player.getUniqueId(), player.getName(), true); // Checks database
                PlayerData playerData = getPlayerData(playerUUID);

                ServerUtil.log("Gonna get the username", Level.WARNING, ServerState.DEBUG);
                ServerUtil.log(playerData.getUsername(), Level.WARNING, ServerState.DEBUG);
                ServerUtil.log(String.valueOf(playerData.getRank().getStaff()), Level.WARNING, ServerState.DEBUG);

                // todo: default values, change to database values
                String playerDataJson = CacheSerializationUtils.serializePlayerData(playerData);
                this.updateAllData(player, playerData);

                ServerUtil.log(">>> Inserted into Jedis: " + playerDataJson, Level.WARNING, ServerState.DEBUG);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Logs relevant information about the player's data.
     *
     * @param playerData The PlayerData object to log.
     */
    private void logPlayerData(PlayerData playerData) {
        if (ServerStateManager.getCurrentState() == ServerState.DEBUG || ServerStateManager.getCurrentState() == ServerState.DEV) {
            Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getRank().getBranchID(Branch.STAFF)));
            Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getVanishState()));
            Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getChannel()));
            Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getCoins().getBalance()));
        }
    }

    @Deprecated
    private void hasDifferentUsername(ProxiedPlayer player) {
        try (Jedis jedis = jedisPool.getResource()) {
            PlayerData deserializedPlayerData = CacheSerializationUtils.deserializePlayerData(jedis.hget("playerdata", player.getUniqueId().toString()));

            if (!deserializedPlayerData.getUsername().equals(player.getName())) {
                logger.info("Username is different");

                PlayerData redisData = getRedisData(player);

                Profile profile = new Profile(redisData.getProfile().getRegistrationDate(), redisData.getProfile().getBio(), redisData.getProfile().getPronouns(), redisData.getProfile().getXenforoId());
                PlayerData playerData = new PlayerData(player.getUniqueId(), player.getName(), redisData.getRank(), profile, redisData.getCoins(), redisData.getChannel(), redisData.getVanishState(), redisData.getCurrentServer());
                jedis.hset("playerdata", player.getUniqueId().toString(), CacheSerializationUtils.serializePlayerData(playerData));
                caffeineCache.put(player.getUniqueId(), playerData);

                Inferris.getInstance().getLogger().warning("Updated username!");
                player.sendMessage(new TextComponent(caffeineCache.getIfPresent(player.getUniqueId()).getUsername()));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private PlayerData createEmpty(ProxiedPlayer player) {
        // Create and return an empty Registry object with default values
        return new PlayerData(player.getUniqueId(), player.getName(),
                new Rank(0, 0, 0, 0),
                new Profile(null, null, null, 0),
                new Coins(36), Channels.NONE, VanishState.DISABLED, Server.LOBBY);
    }

    private PlayerData createEmpty(UUID uuid, String username) {
        // Create and return an empty Registry object with default values
        return new PlayerData(uuid, username,
                new Rank(0, 0, 0, 0),
                new Profile(null, null, null, 0),
                new Coins(36), Channels.NONE, VanishState.DISABLED, Server.LOBBY);
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
