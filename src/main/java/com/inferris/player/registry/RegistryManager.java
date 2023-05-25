package com.inferris.player.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inferris.SerializationModule;
import com.inferris.player.Channels;
import com.inferris.player.vanish.VanishState;
import com.inferris.server.Ports;
import com.inferris.util.CacheSerializationUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class RegistryManager {
    private static RegistryManager instance;
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;


    private RegistryManager() {
        jedisPool = new JedisPool("localhost", Ports.JEDIS.getPort()); // Set Redis server details

        objectMapper = CacheSerializationUtils.createObjectMapper(new SerializationModule());
    }

    public static RegistryManager getInstance() {
        if (instance == null) {
            instance = new RegistryManager();
        }
        return instance;
    }

    public Registry getRegistry(ProxiedPlayer player) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = jedis.get(player.getUniqueId().toString());
            if (json != null) {
                return deserializeRegistry(json);
            } else {
                return createEmptyRegistry(player); // Create an empty Registry object instead of returning null
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPlayer(ProxiedPlayer player, Registry registry) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = serializeRegistry(registry);
            jedis.set(player.getUniqueId().toString(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private Registry createEmptyRegistry(ProxiedPlayer player) {
        // Create and return an empty Registry object with default values
        return new Registry(player.getUniqueId(), player.getName(), Channels.NONE, VanishState.DISABLED);
    }

    public Map<String, Registry> getAllRegistryEntries() {
        Map<String, Registry> registryMap = new HashMap<>();
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> registryJsonMap = jedis.hgetAll("registry");
            for (Map.Entry<String, String> entry : registryJsonMap.entrySet()) {
                String uuid = entry.getKey();
                String registryJson = entry.getValue();
                Registry registry = CacheSerializationUtils.deserializeRegistry(registryJson);
                registryMap.put(uuid, registry);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return registryMap;
    }

    public void deleteRegistry(){
        try (Jedis jedis = RegistryManager.getInstance().getJedisPool().getResource()) {
            jedis.del("registry");
        }

    }

    public void invalidateEntry(UUID uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel("registry", uuid.toString());
        }
    }

    /*
    For new registry entries only
     */
    public void addToRegistryDefault(ProxiedPlayer player) {
        Registry registry = new Registry(player.getUniqueId(), player.getName(), Channels.NONE, VanishState.DISABLED);
        try (Jedis jedis = jedisPool.getResource()) {
            String json = serializeRegistry(registry);
            jedis.set(player.getUniqueId().toString(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String serializeRegistry(Registry registry) throws JsonProcessingException {
        return objectMapper.writeValueAsString(registry);
    }

    private Registry deserializeRegistry(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Registry.class);
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}