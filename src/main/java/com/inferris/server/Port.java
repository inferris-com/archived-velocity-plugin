package com.inferris.server;

public enum Port {
    MYSQL(3306),
    JEDIS(6379);

    private final int port;
    Port(int port){
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
