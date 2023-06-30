package com.inferris.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inferris.player.coins.Coins;
import com.inferris.player.coins.CoinsManager;
import com.inferris.player.registry.Registry;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RankRegistry;
import com.inferris.rank.RanksManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the data associated with a player, including their {@link Registry} information, {@link Rank}, and {@link Coins}.
 *
 * @since 1.0
 */
public class PlayerData implements Serializable {

    private Registry registry;
    private Rank rank;
    private Coins coins;
    private Channels channel;
    private VanishState vanishState;

    public PlayerData(Registry registry, Rank rank, Coins coins, Channels channel, VanishState vanishState){
        this.registry = registry;
        this.rank = rank;
        this.coins = coins;
        this.channel = channel;
        this.vanishState = vanishState;
    }

    PlayerData(){

    }
    public Registry getRegistry() {
        return registry;
    }

    public Rank getRank() {
        return rank;
    }

    public Coins getCoins() {
        return coins;
    }

    public Channels getChannel() {
        return channel;
    }

    public VanishState getVanishState() {
        return vanishState;
    }

    public void setCoins(int amount) {
        CoinsManager.setCoins(ProxyServer.getInstance().getPlayer(getRegistry().getUuid()), amount);
    }

    /**
     * Sets the rank of a player for a specific branch.
     *
     * @param branch the branch for which the rank should be set
     * @param level  the level of the rank to set
     * @since 1.0
     */

    public void setRank(Branch branch, int level) {
        RanksManager.getInstance().setRank(ProxyServer.getInstance().getPlayer(registry.getUuid()), branch, level);
    }

    public void setChannel(Channels channel) {
        this.channel = channel;
    }

    public void setVanishState(VanishState vanishState) {
        this.vanishState = vanishState;
    }

    public int getBranchValue(Branch branch) {
        return rank.getBranchID(branch);
    }

    /**
     * Returns a list of {@link RankRegistry} objects based on the branches of the player's rank.
     * The returned list includes all applicable branches based on the player's rank.
     * @return A list of RankRegistry objects representing the branches of the player's rank.
     * @since 1.0
     */

    @JsonIgnore
    public List<RankRegistry> getByBranches() {
        List<RankRegistry> ranks = new ArrayList<>();
        if (rank.getStaff() == 3) {
            ranks.add(RankRegistry.ADMIN);
        }
        if (rank.getStaff() >= 2) {
            ranks.add(RankRegistry.MOD);
        }
        if (rank.getStaff() >= 1) {
            ranks.add(RankRegistry.HELPER);
        }
        if (rank.getDonor() >= 1) {
            ranks.add(RankRegistry.DONOR);
        }
        return ranks;
    }

    /**
     * Returns the {@link RankRegistry} associated with the highest branch of the player's rank.
     * The returned RankRegistry represents the highest level of authority or privilege based on the player's rank.
     * @return The RankRegistry associated with the highest branch of the player's rank.
     * @since 1.0
     */

    @JsonIgnore
    public RankRegistry getByBranch() {
        int staff = getBranchValue(Branch.STAFF);
        int donor = getBranchValue(Branch.DONOR);
        int other = getBranchValue(Branch.OTHER);

        if (staff == 3) {
            return RankRegistry.ADMIN;
        } else if (staff == 2) {
            return RankRegistry.MOD;
        } else if (staff == 1) {
            return RankRegistry.HELPER;
        } else if (donor == 1) {
            return RankRegistry.DONOR;
        } else {
            return RankRegistry.NONE;
        }
    }
}
