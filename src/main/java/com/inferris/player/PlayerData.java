package com.inferris.player;

import com.inferris.Inferris;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RankRegistry;
import com.inferris.rank.RanksManager;
import com.inferris.util.ConfigUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerData {
    private final ProxiedPlayer player;

    public PlayerData(ProxiedPlayer player){
        this.player = player;
    }

    public int getBranchValue(Branch branch) {
        RanksManager ranksManager = RanksManager.getInstance();
        Rank rank = ranksManager.getRank(player);
        return rank.getBranchID(branch);
    }

    public List<RankRegistry> getByBranches() {
        RanksManager ranksManager = RanksManager.getInstance();
        Rank rank = ranksManager.getRank(player);
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
