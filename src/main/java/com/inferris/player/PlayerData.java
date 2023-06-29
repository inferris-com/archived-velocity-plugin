package com.inferris.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.inferris.player.registry.Registry;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RankRegistry;
import com.inferris.rank.RanksManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerData implements Serializable {

    private Registry registry;
    private Rank rank;
    private Coins coins;

    public PlayerData(Registry registry, Rank rank, Coins coins){
        this.registry = registry;
        this.rank = rank;
        this.coins  =coins;
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

    public int getBranchValue(Branch branch) {
        return rank.getBranchID(branch);
    }

    public void setRank(Branch branch, int level) {
        RanksManager.getInstance().setRank(ProxyServer.getInstance().getPlayer(registry.getUuid()), branch, level);
    }

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
