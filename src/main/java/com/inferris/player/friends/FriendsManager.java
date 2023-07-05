package com.inferris.player.friends;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inferris.Inferris;
import com.inferris.SerializationModule;
import com.inferris.util.CacheSerializationUtils;
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
            return getFriendsFromRedis(playerUUID);
        }
    }

    private Friends getFriendsFromRedis(UUID playerUUID) {
        // Implement the logic to load Friends data from Redis
        // Example: jedisPool.getResource().hget("friends", playerUUID.toString());
        // You can deserialize the stored data using the objectMapper

        // Return a new Friends instance if the data is not found in Redis

        try (Jedis jedis = jedisPool.getResource()) {
            Inferris.getInstance().getLogger().info("Loading Friends from Redis");
            String json = jedis.hget("friends", playerUUID.toString());

            if (json != null) {
                Friends friends = CacheSerializationUtils.deserializeFriends(json);
                Inferris.getInstance().getLogger().info(json);
                caffeineCache.put(playerUUID, friends); // Update Caffeine Cache
                return friends;
            } else {
                return new Friends();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateRedisData(UUID playerUUID, Friends friends){
        try(Jedis jedis = jedisPool.getResource()){
            jedis.hset("friends", playerUUID.toString(), CacheSerializationUtils.serializeFriends(friends));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Cache<UUID, Friends> getCaffeineCache() {
        return caffeineCache;
    }
}
