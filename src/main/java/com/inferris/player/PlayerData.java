package com.inferris.player;

import com.inferris.player.coins.Coins;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Rank;
import com.inferris.server.Server;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents the data associated with a player, including their {@link Profile} information, {@link Rank}, and {@link Coins}.
 *
 * @since 1.0
 */
public class PlayerData implements Serializable {
    private final UUID uuid;
    private String username;
    private final Rank rank;
    private final Profile profile;
    private int coins;
    private Channel channel;
    private VanishState vanishState;
    private Server currentServer;

    public PlayerData(UUID uuid, String username, Rank rank, Profile profile, int coins, Channel channel, VanishState vanishState, Server currentServer) {
        this.uuid = uuid;
        this.username = username;
        this.rank = rank;
        this.profile = profile;
        this.coins = coins;
        this.channel = channel;
        this.vanishState = vanishState;
        this.currentServer = currentServer;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Rank getRank() {
        return rank;
    }

    public Profile getProfile() {
        return profile;
    }

    public int getCoins() {
        return coins;
    }

    public Channel getChannel() {
        return channel;
    }

    public VanishState getVanishState() {
        return vanishState;
    }

    public Server getCurrentServer() {
        return currentServer;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCoins(int amount) {
        this.coins = amount;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setVanishState(VanishState vanishState) {
        this.vanishState = vanishState;
    }

    public void setCurrentServer(Server currentServer) {
        this.currentServer = currentServer;
    }
}