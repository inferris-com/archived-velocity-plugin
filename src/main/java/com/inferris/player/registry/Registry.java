package com.inferris.player.registry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inferris.player.Channels;
import com.inferris.player.vanish.VanishState;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.Serializable;
import java.util.UUID;

public class Registry {
    private UUID uuid;
    private String username;
    private Channels channel;
    private VanishState vanishState;
    public Registry(UUID uuid, String username, Channels channel, VanishState vanishState){
        this.uuid = uuid;
        this.username = username;
        this.channel = channel;
        this.vanishState = vanishState;
    }

    public Registry(){
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

    public Channels setChannel(Channels channels){
        return this.channel = channels;
    }

    public VanishState getVanishState() {
        return vanishState;
    }

    public VanishState setVanishState(VanishState vanishState){
        return this.vanishState = vanishState;
    }
}
