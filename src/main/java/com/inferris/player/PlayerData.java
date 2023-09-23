package com.inferris.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inferris.player.coins.Coins;
import com.inferris.player.coins.CoinsManager;
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
import java.util.UUID;

/**
 * Represents the data associated with a player, including their {@link Registry} information, {@link Rank}, and {@link Coins}.
 *
 * @since 1.0
 */
public class PlayerData implements PlayerDataService, Serializable {
    private UUID uuid;
    private String username;

    private Rank rank;
    private Profile profile;
    private Coins coins;
    private Channels channel;
    private VanishState vanishState;
    private Server currentServer;

    public PlayerData(UUID uuid, String username, Rank rank, Profile profile, Coins coins, Channels channel, VanishState vanishState, Server currentServer) {
        this.uuid = uuid;
        this.username = username;
        this.rank = rank;
        this.profile = profile;
        this.coins = coins;
        this.channel = channel;
        this.vanishState = vanishState;
        this.currentServer = currentServer;
    }

    PlayerData() {

    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
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

    @JsonIgnore
    public ChatColor getNameColor() {
        RankRegistry highestRank = getByBranch();
        return highestRank != RankRegistry.NONE ? highestRank.getColor() : ChatColor.RESET;
    }

    @Override
    public boolean isStaff() {
        return getByBranch().getBranch() == Branch.STAFF;
    }

    public void setCoins(int amount) {
        CoinsManager.setCoins(getUuid(), amount);
    }

    /**
     * Sets the rank of a player for a specific branch.
     *
     * @param branch the branch for which the rank should be set
     * @param level  the level of the rank to set
     * @since 1.0
     */

    public void setRank(Branch branch, int level) {
        RanksManager.getInstance().setRank(getUuid(), branch, level);
    }

    public void setRank(Branch branch, int level, boolean hasMessage) {
        RanksManager.getInstance().setRank(uuid, branch, level);
        if (ProxyServer.getInstance().getPlayer(uuid) != null) {
            if (ProxyServer.getInstance().getPlayer(getUuid()).isConnected()) {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(getUuid());
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
     * Retrieves the top ranks from each branch (staff and donor) for the player.
     * Only the highest rank from each branch will be included in the list.
     *
     * @return A list of the top ranks from each branch.
     */

    @JsonIgnore
    public List<RankRegistry> getApplicableRanks() {
        List<RankRegistry> ranks = new ArrayList<>();
        int staff = rank.getStaff();
        int builder = rank.getBuilder();
        int donor = rank.getDonor();

        switch (staff) {
            case 4,3 -> ranks.add(RankRegistry.ADMIN);
            case 2 -> ranks.add(RankRegistry.MOD);
            case 1 -> ranks.add(RankRegistry.HELPER);
        }

        if (builder == 1) {
            ranks.add(RankRegistry.BUILDER);
        }

        if (donor == 1) {
            ranks.add(RankRegistry.DONOR);
        }

        return ranks;
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
        int builder = getBranchValue(Branch.BUILDER);
        int donor = getBranchValue(Branch.DONOR);

        if (staff >=3) {
            return RankRegistry.ADMIN;
        } else if (staff == 2) {
            return RankRegistry.MOD;
        } else if (staff == 1) {
            return RankRegistry.HELPER;
        } else if (builder == 1) {
            return RankRegistry.BUILDER;
        } else if (donor == 1) {
            return RankRegistry.DONOR;
        } else {
            return RankRegistry.NONE;
        }
    }
}
