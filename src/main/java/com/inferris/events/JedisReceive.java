package com.inferris.events;

import com.inferris.Inferris;
import com.inferris.server.JedisChannels;
import redis.clients.jedis.JedisPubSub;

public class JedisReceive extends JedisPubSub {
    @Override
    public void onMessage(String channel, String message) {
        Inferris.getInstance().getLogger().severe("Triggered JedisPlayerDataEvent");
        if (channel.equals(JedisChannels.SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE.name())) {
        }
    }
}
