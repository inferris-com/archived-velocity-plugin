package com.inferris.events.redis;

import com.inferris.events.redis.dispatching.JedisEventHandler;

public class EventGenericFlex implements JedisEventHandler {
    @Override
    public void handle(String message, String senderId) {
        EventPayload payload = EventPayload.fromPayloadString(message);
        if (senderId.equals(payload.getSenderId())) {
        }
    }
}
