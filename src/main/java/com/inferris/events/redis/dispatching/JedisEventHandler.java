package com.inferris.events.redis.dispatching;

public interface JedisEventHandler {
    void handle(String message);
}
