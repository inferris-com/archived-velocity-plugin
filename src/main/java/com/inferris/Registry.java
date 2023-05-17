package com.inferris;

import java.io.Serializable;
import java.util.UUID;

public class Registry implements Serializable {
    private final UUID uuid;
    private final String username;
    private final Channels channel;
    public Registry(UUID uuid, String username, Channels channel){
        this.uuid = uuid;
        this.username = username;
        this.channel = channel;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public Channels getChannel() {
        return channel;
    }
}
