package com.inferris.events.redis;

import com.inferris.events.redis.dispatching.JedisEventHandler;

public class EventPlayerFlex implements JedisEventHandler {
    @Override
    public void handle(String message, String senderId) {
    }
}
