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
import com.inferris.server.Server;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the data associated with a player, including their {@link Registry} information, {@link Rank}, and {@link Coins}.
 *
 * @since 1.0
 */
public class PlayerData implements PlayerDataService, Serializable {

    private Registry registry;
    private Rank rank;
    private Profile profile;
    private Coins coins;
    private Channels channel;
    private VanishState vanishState;
    private Server currentServer;

    public PlayerData(Registry registry, Rank rank, Profile profile, Coins coins, Channels channel, VanishState vanishState, Server currentServer) {
        this.registry = registry;
        this.rank = rank;
        this.profile = profile;
        this.coins = coins;
        this.channel = channel;
        this.vanishState = vanishState;
        this.currentServer = currentServer;
    }

    PlayerData() {

    }

    public Registry getRegistry() {
        return registry;
    }

    public Rank getRank() {
        return rank;
    }

    public Profile getProfile() {
        return profile;
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

    public Server getCurrentServer() {
        return currentServer;
    }

    @Override
    public ChatColor getNameColor() {
        return switch (getByBranch()) {
            case ADMIN -> RankRegistry.ADMIN.getColor();
            case MOD -> RankRegistry.MOD.getColor();
            case HELPER -> RankRegistry.HELPER.getColor();
            case DONOR -> RankRegistry.DONOR.getColor();
            default -> ChatColor.RESET;
        };
    }

    @Override
    public boolean isStaff() {
        return getByBranch().getBranch() == Branch.STAFF;
    }

    public void setCoins(int amount) {
        CoinsManager.setCoins(registry.getUuid(), amount);
    }

    /**
     * Sets the rank of a player for a specific branch.
     *
     * @param branch the branch for which the rank should be set
     * @param level  the level of the rank to set
     * @since 1.0
     */

    public void setRank(Branch branch, int level) {
        RanksManager.getInstance().setRank(registry.getUuid(), branch, level);
    }

    public void setRank(Branch branch, int level, boolean hasMessage) {
        RanksManager.getInstance().setRank(registry.getUuid(), branch, level);
        if (ProxyServer.getInstance().getPlayer(registry.getUuid()) != null) {
            if (ProxyServer.getInstance().getPlayer(registry.getUuid()).isConnected()) {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(registry.getUuid());
                if (hasMessage) player.sendMessage(new TextComponent(ChatColor.GREEN + "Your rank has been set"));
            }
        }
    }

    public void setChannel(Channels channel) {
        this.channel = channel;
    }

    public void setVanishState(VanishState vanishState) {
        this.vanishState = vanishState;
    }

    public void setCurrentServer(Server currentServer) {
        this.currentServer = currentServer;
    }

    public int getBranchValue(Branch branch) {
        return rank.getBranchID(branch);
    }

    /**
     * Returns a list of {@link RankRegistry} objects based on the branches of the player's rank.
     * The returned list includes all applicable branches based on the player's rank.
     *
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
     * Retrieves the top ranks from each branch (staff and donor) for the player.
     * Only the highest rank from each branch will be included in the list.
     *
     * @return A list of the top ranks from each branch.
     */

    @JsonIgnore
    public List<RankRegistry> getTopRanksByBranches() {
        List<RankRegistry> ranks = new ArrayList<>();

        if (rank.getStaff() >= 3) {
            ranks.add(RankRegistry.ADMIN);
        } else if (rank.getStaff() >= 2) {
            ranks.add(RankRegistry.MOD);
        } else if (rank.getStaff() >= 1) {
            ranks.add(RankRegistry.HELPER);
        }

        if (rank.getDonor() >= 1) {
            ranks.add(RankRegistry.DONOR);
        }
        return ranks;
    }

    @JsonIgnore
    public String formatRankList(List<RankRegistry> ranks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ranks.size(); i++) {
            RankRegistry rank = ranks.get(i);
            sb.append(rank.getPrefix());
            if (i < ranks.size() - 1) {
                sb.append(ChatColor.RESET).append(", ");
            }
        }
        return sb.toString();
    }


    /**
     * Returns the {@link RankRegistry} associated with the highest branch of the player's rank.
     * The returned RankRegistry represents the highest level of authority or privilege based on the player's rank.
     *
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
