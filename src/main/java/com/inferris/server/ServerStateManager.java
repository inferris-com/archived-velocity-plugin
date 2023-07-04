package com.inferris.server;

import com.inferris.Inferris;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ServerStateManager {
    private static ServerState currentState = ServerState.NORMAL;
    private static JedisPool jedisPool = Inferris.getJedisPool();

    public static ServerState getCurrentState() {
        return currentState;
    }

    public static void setCurrentState(ServerState currentState) {
        ServerStateManager.currentState = currentState;
        setJedisState(currentState);
    }

    public static void setJedisState(ServerState serverState){
        try(Jedis jedis = jedisPool.getResource()){
            jedis.hset("server", "state", serverState.name().toLowerCase());
        }
    }
}