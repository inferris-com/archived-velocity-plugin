package com.inferris.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inferris.Inferris;
import com.inferris.SerializationModule;
import com.inferris.player.registry.Registry;
import com.inferris.player.registry.RegistryManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Rank;
import com.inferris.rank.RanksManager;
import com.inferris.server.Ports;
import com.inferris.util.CacheSerializationUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class PlayerDataManager {
    private static PlayerDataManager instance;
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    private PlayerDataManager() {
        jedisPool = new JedisPool("localhost", Ports.JEDIS.getPort()); // Set Redis server details

        objectMapper = CacheSerializationUtils.createObjectMapper(new SerializationModule());
    }

    public static PlayerDataManager getInstance() {
        if (instance == null) {
            instance = new PlayerDataManager();
        }
        return instance;
    }

    public PlayerData getData(ProxiedPlayer player) {
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

    private PlayerData createEmpty(ProxiedPlayer player) {
        // Create and return an empty Registry object with default values
        return new PlayerData(new Registry(player.getUniqueId(), player.getName(), Channels.NONE, VanishState.DISABLED),
                new Rank(0, 0, 0),
                new Coins(36));
    }

    public void invalidateEntry() {
        try (Jedis jedis = getJedisPool().getResource()) {
            jedis.del("playerdata");
        }
    }

    public void checkJoinedBefore(ProxiedPlayer player) {
        Inferris.getInstance().getLogger().warning("Checking join");

        /*
        Master playerdata object setting
         */

        try (Jedis jedis = getJedisPool().getResource()) {
            if(jedis.hexists("playerdata", player.getUniqueId().toString())){
                Inferris.getInstance().getLogger().warning("Exists");
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
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}
