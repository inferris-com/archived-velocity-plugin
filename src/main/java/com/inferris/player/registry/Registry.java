package com.inferris.player.registry;

import com.inferris.player.Channels;
import com.inferris.player.vanish.VanishState;

import java.util.UUID;

/**
 * Represents the registry information of a player, including their UUID, username, channel, and vanish state.
 * This class is primarily used within the {@link com.inferris.player.PlayerData} class, which represents the data associated with a player.
 *
 * <p>The registry data is cached and serialized/deserialized for network transmission between Bungee plugin and Spigot using Redis.</p>
 *
 * @since 1.0
 */
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
