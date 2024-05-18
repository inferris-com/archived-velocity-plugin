package com.inferris.events.redis.dispatching;

import redis.clients.jedis.JedisPubSub;

public class DispatchingJedisPubSub extends JedisPubSub {
    private final JedisEventDispatcher dispatcher;

    public DispatchingJedisPubSub(JedisEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void onMessage(String channel, String message) {
        dispatcher.dispatch(channel, message);
    }

}
