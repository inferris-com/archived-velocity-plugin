package com.inferris.events;

import com.inferris.Inferris;
import redis.clients.jedis.JedisPubSub;

public class JedisReceive extends JedisPubSub {
    @Override
    public void onMessage(String channel, String message) {
        Inferris.getInstance().getLogger().severe("Triggered JedisPlayerDataEvent");

    }
}
