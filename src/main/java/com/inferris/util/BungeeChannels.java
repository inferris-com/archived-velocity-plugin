package com.inferris.util;

public enum BungeeChannels {
    BUNGEECORD("BungeeCord"),
    STAFFCHAT("inferris:staffchat"),
    PLAYER_REGISTRY("inferris:player_registry"),
    DIRECT_MESSAGE("inferris:direct_message");

    private final String name;
    BungeeChannels(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
