package com.inferris.server.jedis;

import com.inferris.Inferris;
import com.inferris.events.redis.EventPayload;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisHelper {

    private static final JedisPool jedisPool = Inferris.getJedisPool();

    // Method to easily perform an HSET operation
    public static boolean hsetWithStatus(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(key, field, value);
            return true;
        } catch (Exception e) {
            // Handle logging or specific exceptions here
            System.err.println("Error performing HSET operation: " + e.getMessage());
            return false;
        }
    }

    public static void hset(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(key, field, value);
        } catch (Exception e) {
            // Handle logging or specific exceptions here
            System.err.println("Error performing HSET operation: " + e.getMessage());
        }
    }

    public static void hdel(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel(key, field);
        } catch (Exception e) {
            // Handle logging or specific exceptions here
            System.err.println("Error performing HSET operation: " + e.getMessage());
        }
    }

    public static void publish(JedisChannel channel, EventPayload eventPayload) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel.getChannelName(), eventPayload.toPayloadString());
        }
    }

    public static void publish(JedisChannel channel, String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel.getChannelName(), message);
        }
    }
}