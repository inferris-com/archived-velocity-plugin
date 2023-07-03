package com.inferris.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.inferris.Inferris;
import com.inferris.SerializationModule;
import com.inferris.player.coins.Coins;
import com.inferris.player.registry.Registry;
import com.inferris.player.registry.RegistryManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RanksManager;;
import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;
import com.inferris.util.CacheSerializationUtils;
import com.inferris.util.ServerUtil;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

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
            return getRedisData(player);
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
                return createEmpty(player); // Create an empty Registry object instead of returning null
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public PlayerData getRedisData(UUID uuid, String username) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.hget("playerdata", uuid.toString());
            if (json != null) {
                return CacheSerializationUtils.deserializePlayerData(json);
            } else {
                return createEmpty(uuid, username); // Create an empty Registry object instead of returning null
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
     Retrieves the UUID associated with a given username from player data stored in Redis.
     @param username The username to search for.
     @return The UUID corresponding to the provided username, or null if no match is found.
     */

    public UUID getUUIDByUsername(String username) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> playerDataEntries = jedis.hgetAll("playerdata");
            Gson gson = new Gson();

            for (Map.Entry<String, String> entry : playerDataEntries.entrySet()) {
                String uuid = entry.getKey();

                // Parse the value as JSON to access the username field
                try {
                    JsonElement jsonElement = gson.fromJson(entry.getValue(), JsonElement.class);
                    String entryUsername = jsonElement.getAsJsonObject().getAsJsonObject("registry").get("username").getAsString();

                    if (entryUsername.equalsIgnoreCase(username)) {
                        // Match found, return the UUID
                        return UUID.fromString(uuid);
                    }
                } catch (Exception ignored) {
                }
            }
            // No match found for the username
            return null;
        }
    }

    /**
     Checks if a given username has a corresponding UUID in the player data stored in Redis.
     @param username The username to check.
     @return {@code true} if a match is found for the username, {@code false} otherwise.
     */

    public boolean hasUUIDByUsername(String username) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> playerDataEntries = jedis.hgetAll("playerdata");
            Gson gson = new Gson();
            for (String value : playerDataEntries.values()) {
                // Parse the value as JSON to access the username field
                JsonElement jsonElement = gson.fromJson(value, JsonElement.class);
                String entryUsername = jsonElement.getAsJsonObject().getAsJsonObject("registry").get("username").getAsString();

                if (entryUsername.equalsIgnoreCase(username)) {
                    // Match found for the username
                    return true;
                }
            }
            // No match found for the username
            return false;
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
            jedis.hset("playerdata", player.getUniqueId().toString(), CacheSerializationUtils.serializePlayerData(playerData));
            updateCaffeineCache(player, playerData);
            Inferris.getInstance().getLogger().info("Updated Redis information via Jedis. Caches updated!");
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

    private void updateCaffeineCache(ProxiedPlayer player, PlayerData playerData) {
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

    /**
     * Checks if the provided player has joined before by looking up their data in Redis and caching if necessary.
     *
     * @param player The ProxiedPlayer object representing the player to check.
     */
    public void checkJoinedBefore(ProxiedPlayer player) {
        boolean isDebug = ServerStateManager.getCurrentState() == ServerState.DEBUG;

        ServerUtil.log("Checking join", Level.WARNING, ServerState.DEBUG);

        try (Jedis jedis = getJedisPool().getResource()) {
            UUID playerUUID = player.getUniqueId();

            if (jedis.hexists("playerdata", playerUUID.toString())) {
                ServerUtil.log("Exists", Level.WARNING, ServerState.DEBUG);
                hasDifferentUsername(player);

                if (caffeineCache.getIfPresent(playerUUID) == null) {
                    String playerDataJson = jedis.hget("playerdata", playerUUID.toString());
                    PlayerData playerData = CacheSerializationUtils.deserializePlayerData(playerDataJson);
                    caffeineCache.put(playerUUID, playerData);
                    logPlayerData(playerData);
                }
            } else {
                ServerUtil.log("Not in registry, caching", Level.WARNING, ServerState.DEBUG);

                Rank rank = RanksManager.getInstance().getRank(player);
                Registry registry = RegistryManager.getInstance().getRegistry(player, rank);

                PlayerData playerData = new PlayerData(registry, rank, new Profile(null, null, LocalDate.now()), new Coins(36), Channels.NONE, VanishState.DISABLED);
                String playerDataJson = CacheSerializationUtils.serializePlayerData(playerData);
                jedis.hset("playerdata", playerUUID.toString(), playerDataJson);

                ServerUtil.log(">>> " + playerDataJson, Level.SEVERE, ServerState.DEBUG);


                if (caffeineCache.getIfPresent(playerUUID) != null) {
                    PlayerData cachedData = CacheSerializationUtils.deserializePlayerData(playerDataJson);
                    caffeineCache.put(playerUUID, cachedData);
                }
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
        Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getRank().getBranchID(Branch.STAFF)));
        Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getVanishState()));
        Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getChannel()));
        Inferris.getInstance().getLogger().severe(playerData.getRegistry().getUsername());
        Inferris.getInstance().getLogger().severe(String.valueOf(playerData.getCoins().getBalance()));
    }


    private void hasDifferentUsername(ProxiedPlayer player) {
        try (Jedis jedis = jedisPool.getResource()) {
            PlayerData deserializedPlayerData = CacheSerializationUtils.deserializePlayerData(jedis.hget("playerdata", player.getUniqueId().toString()));

            if (!deserializedPlayerData.getRegistry().getUsername().equals(player.getName())) {
                logger.info("Username is different");

                PlayerData redisData = getRedisData(player);

                Registry registry = new Registry(player.getUniqueId(), player.getName());
                Profile profile = new Profile(redisData.getProfile().getBio(), redisData.getProfile().getPronouns(), redisData.getProfile().getRegistrationDate());
                PlayerData playerData = new PlayerData(registry, redisData.getRank(), profile, redisData.getCoins(), redisData.getChannel(), redisData.getVanishState());
                jedis.hset("playerdata", player.getUniqueId().toString(), CacheSerializationUtils.serializePlayerData(playerData));
                caffeineCache.put(player.getUniqueId(), playerData);

                Inferris.getInstance().getLogger().warning("Updated username!");
                player.sendMessage(new TextComponent(caffeineCache.getIfPresent(player.getUniqueId()).getRegistry().getUsername()));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private PlayerData createEmpty(ProxiedPlayer player) {
        // Create and return an empty Registry object with default values
        return new PlayerData(new Registry(player.getUniqueId(), player.getName()),
                new Rank(0, 0, 0),
                new Profile(null, null, null),
                new Coins(36), Channels.NONE, VanishState.DISABLED);
    }

    private PlayerData createEmpty(UUID uuid, String username) {
        // Create and return an empty Registry object with default values
        return new PlayerData(new Registry(uuid, username),
                new Rank(0, 0, 0),
                new Profile(null, null, null),
                new Coins(36), Channels.NONE, VanishState.DISABLED);
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
