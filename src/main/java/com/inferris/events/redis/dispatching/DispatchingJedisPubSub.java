package com.inferris.events.redis.dispatching;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.events.redis.EventPayload;
import redis.clients.jedis.JedisPubSub;

public class DispatchingJedisPubSub extends JedisPubSub {
    private final JedisEventDispatcher dispatcher;
    private final String instanceId;

    public DispatchingJedisPubSub(JedisEventDispatcher dispatcher, String instanceId) {
        this.dispatcher = dispatcher;
        this.instanceId = instanceId;
    }

    @Override
    public void onMessage(String channel, String message) {
        EventPayload payload = null;
            payload = EventPayload.fromPayloadString(message);

        if (!instanceId.equals(payload.getSenderId())) {
            dispatcher.dispatch(channel, message, payload.getSenderId());
        }
    }
}