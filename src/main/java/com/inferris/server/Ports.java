package com.inferris.server;

public enum Ports {
    MYSQL(3306),
    JEDIS(6379);

    private final int port;
    Ports(int port){
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
