package com.inferris.server;

public enum Servers {
    PROXY(25565),
    LOBBY(25566),
    TEST(25567);

    private final int port;
    Servers(int port){
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
