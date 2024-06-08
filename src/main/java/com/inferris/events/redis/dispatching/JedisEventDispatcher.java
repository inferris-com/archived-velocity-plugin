package com.inferris.events.redis.dispatching;

import java.util.HashMap;
import java.util.Map;

public class JedisEventDispatcher {
    private final Map<String, JedisEventHandler> eventHandlers = new HashMap<>();

    public void registerHandler(String channel, JedisEventHandler handler) {
        eventHandlers.put(channel, handler);
    }

    public void dispatch(String channel, String message, String senderId) {
        JedisEventHandler handler = eventHandlers.get(channel);
        if (handler != null) {
            handler.handle(message, senderId);
        } else {
            System.err.println("No handler registered for channel: " + channel);
        }
    }
}