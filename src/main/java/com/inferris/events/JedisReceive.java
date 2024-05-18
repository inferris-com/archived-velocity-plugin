package com.inferris.events;

import redis.clients.jedis.JedisPubSub;

@Deprecated
public class JedisReceive extends JedisPubSub {

    public JedisReceive(){
    }

    @Override
    public void onMessage(String channel, String message) {
    }
}
