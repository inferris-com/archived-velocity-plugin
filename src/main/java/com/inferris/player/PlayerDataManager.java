package com.inferris.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inferris.Inferris;
import com.inferris.SerializationModule;
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

import java.util.UUID;

public class PlayerDataManager {

    /*
    This class is responsible for storing a master PlayerData object, which includes registry and rank info.
    We are storing PlayerData (Caffeine) cache information from Redis, so we have an additional data layer

    #checkJoinedBefore() includes the caching logic
     */

    private static PlayerDataManager instance;
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;
    private final Cache<UUID, PlayerData> cache;

    private PlayerDataManager() {
        jedisPool = new JedisPool("localhost", Ports.JEDIS.getPort()); // Set Redis server details
        objectMapper = CacheSerializationUtils.createObjectMapper(new SerializationModule());
        cache = Caffeine.newBuilder().build();
    }

    public static PlayerDataManager getInstance() {
        if (instance == null) {
            instance = new PlayerDataManager();
        }
        return instance;
    }

    public PlayerData getPlayerData(ProxiedPlayer player) {
        if(cache.getIfPresent(player.getUniqueId()) != null){
            return cache.asMap().get(player.getUniqueId());
        }else{
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

    /*
    We store them in the PlayerData cache here
     */
    public void checkJoinedBefore(ProxiedPlayer player) {
        Inferris.getInstance().getLogger().warning("Checking join");

        /*
        Master playerdata object setting
         */

        try (Jedis jedis = getJedisPool().getResource()) {
            if(jedis.hexists("playerdata", player.getUniqueId().toString())){
                Inferris.getInstance().getLogger().warning("Exists");

                if(cache.asMap().get(player.getUniqueId()) == null){
                    cache.asMap().put(player.getUniqueId(), CacheSerializationUtils.deserializePlayerData(jedis.hget("playerdata", player.getUniqueId().toString())));
                    Inferris.getInstance().getLogger().severe(cache.asMap().get(player.getUniqueId()).getRank().getBranchID(Branch.STAFF) + "");
                    Inferris.getInstance().getLogger().severe(cache.asMap().get(player.getUniqueId()).getRegistry().getVanishState() + "");
                    Inferris.getInstance().getLogger().severe(cache.asMap().get(player.getUniqueId()).getRegistry().getChannel() + "");
                    Inferris.getInstance().getLogger().severe(cache.asMap().get(player.getUniqueId()).getRegistry().getUsername() + "");
                    Inferris.getInstance().getLogger().severe(cache.asMap().get(player.getUniqueId()).getCoins().getBalance() + "");
                }
            }else{
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

                if(cache.asMap().get(player.getUniqueId()) != null){
                    cache.asMap().put(player.getUniqueId(), CacheSerializationUtils.deserializePlayerData(jedis.hget("playerdata", player.getUniqueId().toString())));
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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

    public void invalidateCache(ProxiedPlayer player){
        cache.invalidate(player.getUniqueId());
    }

    public Cache<UUID, PlayerData> getCache() {
        return cache;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}
