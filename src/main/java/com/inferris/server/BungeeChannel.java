package com.inferris.server;

public enum BungeeChannel {
    BUNGEECORD("BungeeCord"),
    STAFFCHAT("inferris:staffchat"),
    PLAYER_REGISTRY("inferris:player_registry"),
    DIRECT_MESSAGE("inferris:direct_message");

    private final String name;
    BungeeChannel(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
