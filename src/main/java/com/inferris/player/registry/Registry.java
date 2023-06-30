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

    public Registry(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public Registry() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }
}