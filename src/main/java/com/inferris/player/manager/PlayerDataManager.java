/*
 * Copyright (c) 2024. Inferris.
 * All rights reserved.
 */

package com.inferris.player.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.inferris.Inferris;
import com.inferris.events.redis.EventPayload;
import com.inferris.events.redis.PlayerAction;
import com.inferris.player.Profile;
import com.inferris.player.channel.Channel;
import com.inferris.player.PlayerData;
import com.inferris.player.repository.PlayerDataRepository;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.server.Server;
import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;
import com.inferris.server.jedis.JedisChannel;
import com.inferris.util.SerializationUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    private final JedisPool jedisPool = Inferris.getJedisPool();
    private final ObjectMapper objectMapper;
    private final Cache<UUID, PlayerData> caffeineCache;
   private final Provider<PlayerDataRepository> playerDataRepositoryProvider;


    @Inject
    public PlayerDataManager(ObjectMapper objectMapper, Provider<PlayerDataRepository> playerDataRepositoryProvider) {
        this.objectMapper = objectMapper;
        this.playerDataRepositoryProvider = playerDataRepositoryProvider;

        caffeineCache = Caffeine.newBuilder().build();
    }

    private PlayerDataRepository getPlayerDataRepository() {
        return playerDataRepositoryProvider.get();
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

        return CompletableFuture.supplyAsync(() -> {
            return getPlayerData(uuid);
        }, Inferris.getInstance().getExecutorService());  // Assuming you have an Executor for async tasks
    }

    public PlayerData getRedisData(ProxiedPlayer player, boolean setCache) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.hget("playerdata", player.getUniqueId().toString());
            if (json != null) {
                if(setCache)
                    caffeineCache.put(player.getUniqueId(), SerializationUtils.deserializePlayerData(json));
                return SerializationUtils.deserializePlayerData(json);
            } else {
                Inferris.getInstance().getLogger().severe("Trying getPlayerDataFromDatabase");
                return getPlayerDataRepository().getPlayerDataFromDatabase(player.getUniqueId());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
                return SerializationUtils.deserializePlayerData(json);
            } else {
                Inferris.getInstance().getLogger().severe("Trying getPlayerDataFromDatabase");
                return getPlayerDataRepository().getPlayerDataFromDatabase(player.getUniqueId());
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
                return getPlayerDataRepository().getPlayerDataFromDatabase(uuid);
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

            jedis.publish(JedisChannel.PLAYERDATA_UPDATE.getChannelName(), new EventPayload(player.getUniqueId(),
                    PlayerAction.UPDATE_PLAYER_DATA,
                    null,
                    Inferris.getInstanceId()).toPayloadString());
            Inferris.getInstance().getLogger().severe("Published update for UUID: " + uuid);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
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

    public void invalidateCache(UUID uuid) {
        caffeineCache.invalidate(uuid);
    }

    public Cache<UUID, PlayerData> getCache() {
        return caffeineCache;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}