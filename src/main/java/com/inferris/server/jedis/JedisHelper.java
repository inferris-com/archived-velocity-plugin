package com.inferris.server.jedis;

import com.inferris.Inferris;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisHelper {

    private final JedisPool jedisPool;

    public JedisHelper(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    // Method to easily perform an HSET operation
    public boolean hsetWithStatus(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(key, field, value);
            return true;
        } catch (Exception e) {
            // Handle logging or specific exceptions here
            System.err.println("Error performing HSET operation: " + e.getMessage());
            return false;
        }
    }

    public void hset(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(key, field, value);
        } catch (Exception e) {
            // Handle logging or specific exceptions here
            System.err.println("Error performing HSET operation: " + e.getMessage());
        }
    }

    public void publish(JedisChannels channel, String message){
        try(Jedis jedis = jedisPool.getResource()){
            jedis.publish(channel.getChannelName(), message);
        }
    }
}