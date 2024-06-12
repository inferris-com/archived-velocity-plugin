package com.inferris.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisBuilder {
    private Jedis jedis;
    private JedisPool pool;
    private JedisPoolConfig poolConfig;
    private String address;
    private int port;
    private String password;

    public JedisBuilder(String address, int port) {
        jedis = new Jedis();
        poolConfig = new JedisPoolConfig();
        this.address = address;
        this.port = port;
    }

    public JedisBuilder setDefaultConfig(){
        poolConfig.setMaxTotal(128); // Maximum number of connections
        poolConfig.setMaxIdle(64); // Maximum number of idle connections
        poolConfig.setMinIdle(16); // Minimum number of idle connections
        poolConfig.setTestOnBorrow(true); // Validate connection before borrowing
        poolConfig.setTestOnReturn(true); // Validate connection before returning
        poolConfig.setTestWhileIdle(true); // Validate connections in the background
        poolConfig.setMinEvictableIdleTimeMillis(60000); // Minimum time a connection can sit idle before being eligible for eviction
        poolConfig.setTimeBetweenEvictionRunsMillis(30000); // Time between eviction runs
        poolConfig.setNumTestsPerEvictionRun(-1); // Number of connections to test per eviction run
        return this;
    }

    public JedisBuilder setPassword(String password){
        this.password = password;
        return this;
    }

    public JedisPool build() {
        if (password != null) {
            pool = new JedisPool(poolConfig, address, port, 2000, password);
        } else {
            pool = new JedisPool(poolConfig, address, port);
        }
        return pool;
    }
}
