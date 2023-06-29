package com.inferris.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inferris.Inferris;
import com.inferris.SerializationModule;
import com.inferris.database.DatabasePool;
import com.inferris.player.registry.Registry;
import com.inferris.player.registry.RegistryManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RanksManager;
import com.inferris.server.Ports;
import com.inferris.util.CacheSerializationUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class PlayerDataManager {

    /*
    This class is responsible for storing a master PlayerData object, which includes registry and rank info.
    We are storing PlayerData (Caffeine) cache information from Redis, so we have an additional data layer

    #checkJoinedBefore() includes the caching logic
     */

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

    public PlayerData getPlayerData(ProxiedPlayer player) {
        if (caffeineCache.getIfPresent(player.getUniqueId()) != null) {
            return caffeineCache.asMap().get(player.getUniqueId());
        } else {
            return getRedisData(player);
        }
    }

    public PlayerData getRedisData(ProxiedPlayer player) {
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

    public void updateRedisData(ProxiedPlayer player, PlayerData playerData){
        try(Jedis jedis = jedisPool.getResource()){
            jedis.hset("playerdata", player.getUniqueId().toString(), CacheSerializationUtils.serializePlayerData(playerData));
            caffeineCache.put(player.getUniqueId(), playerData);
            Inferris.getInstance().getLogger().info("Updated Redis information via Jedis. Caches updated!");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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

    /*
    We store them in the PlayerData cache here
     */
    public void checkJoinedBefore(ProxiedPlayer player) {
        Inferris.getInstance().getLogger().warning("Checking join");

        /*
        Master playerdata object setting
            Reminder: Redis cache is PERSISTENT
         */

        try (Jedis jedis = getJedisPool().getResource()) {

            if (jedis.hexists("playerdata", player.getUniqueId().toString())) {
                Inferris.getInstance().getLogger().warning("Exists");
                hasDifferentUsername(player);

                /* If in Redis, but not caffeine cache */

                if (caffeineCache.asMap().get(player.getUniqueId()) == null) {
                    caffeineCache.asMap().put(player.getUniqueId(), CacheSerializationUtils.deserializePlayerData(jedis.hget("playerdata", player.getUniqueId().toString())));
                    Inferris.getInstance().getLogger().severe(String.valueOf(caffeineCache.asMap().get(player.getUniqueId()).getRank().getBranchID(Branch.STAFF)));
                    Inferris.getInstance().getLogger().severe(String.valueOf(caffeineCache.asMap().get(player.getUniqueId()).getRegistry().getVanishState()));
                    Inferris.getInstance().getLogger().severe(String.valueOf(caffeineCache.asMap().get(player.getUniqueId()).getRegistry().getChannel()));
                    Inferris.getInstance().getLogger().severe(caffeineCache.asMap().get(player.getUniqueId()).getRegistry().getUsername());
                    Inferris.getInstance().getLogger().severe(String.valueOf(caffeineCache.asMap().get(player.getUniqueId()).getCoins().getBalance()));
                }
            } else {
                Inferris.getInstance().getLogger().warning("Not in registry, caching");

                /*
                Check if they are in the database.  Normal database caching
                 */

                Rank rank = RanksManager.getInstance().getRank(player); // Finished
                Registry registry = RegistryManager.getInstance().getRegistry(player, rank); // todo

                PlayerData playerData = new PlayerData(registry, rank, new Coins(36));

                String json = CacheSerializationUtils.serializePlayerData(playerData);
                jedis.hset("playerdata", player.getUniqueId().toString(), json);

                Inferris.getInstance().getLogger().severe(">>>> " + json);

                if (caffeineCache.asMap().get(player.getUniqueId()) != null) {
                    caffeineCache.asMap().put(player.getUniqueId(), CacheSerializationUtils.deserializePlayerData(jedis.hget("playerdata", player.getUniqueId().toString())));
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void hasDifferentUsername(ProxiedPlayer player) {
        try (Jedis jedis = jedisPool.getResource()) {
            PlayerData deserializedPlayerData = CacheSerializationUtils.deserializePlayerData(jedis.hget("playerdata", player.getUniqueId().toString()));

            if (!deserializedPlayerData.getRegistry().getUsername().equals(player.getName())) {
                logger.info("Username is different");

                PlayerData redisData = getRedisData(player);

                Registry registry = new Registry(player.getUniqueId(), player.getName(), redisData.getRegistry().getChannel(), redisData.getRegistry().getVanishState());
                PlayerData playerData = new PlayerData(registry, redisData.getRank(), redisData.getCoins());
                jedis.hset("playerdata", player.getUniqueId().toString(), CacheSerializationUtils.serializePlayerData(playerData));
                caffeineCache.put(player.getUniqueId(), playerData);

                Inferris.getInstance().getLogger().warning("Updated username!");
                player.sendMessage(caffeineCache.getIfPresent(player.getUniqueId()).getRegistry().getUsername());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private PlayerData createEmpty(ProxiedPlayer player) {
        // Create and return an empty Registry object with default values
        return new PlayerData(new Registry(player.getUniqueId(), player.getName(), Channels.NONE, VanishState.DISABLED),
                new Rank(0, 0, 0),
                new Coins(36));
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
