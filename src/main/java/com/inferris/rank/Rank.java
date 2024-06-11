package com.inferris.rank;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Rank {
    private int staff;
    private int builder;
    private int donor;
    private int other;

    public Rank(int staff, int builder, int donor, int other) {
        this.staff = staff;
        this.builder = builder;
        this.donor = donor;
        this.other = other;
    }

    public Rank(){
    }

    public int getStaff() {
        return staff;
    }

    public int getBuilder() {
        return builder;
    }

    public int getDonor() {
        return donor;
    }

    public int getOther() {
        return other;
    }

    public void setStaff(int staff) {
        this.staff = staff;
    }

    public void setBuilder(int builder) {
        this.builder = builder;
    }

    public void setDonor(int donor) {
        this.donor = donor;
    }

    public void setOther(int other) {
        this.other = other;
    }

    public int getBranchValue(Branch branch) {
        return getBranchID(branch);
    }

    public int getBranchID(Branch branch) {
        return switch (branch) {
            case STAFF -> staff;
            case BUILDER -> builder;
            case DONOR -> donor;
            case OTHER -> other;
            default -> -1;
        };
    }

    public RankRegistry getByBranch() {
        int staff = getBranchValue(Branch.STAFF);
        int builder = getBranchValue(Branch.BUILDER);
        int donor = getBranchValue(Branch.DONOR);

        if (staff == 3) {
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

    public List<RankRegistry> getApplicableRanks() {
        List<RankRegistry> ranks = new ArrayList<>();
        int staff = getStaff();
        int builder = getBuilder();
        int donor = getDonor();

        switch (staff) {
            case 4, 3 -> ranks.add(RankRegistry.ADMIN);
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

    public String getFormattedApplicableRanks() {
        List<RankRegistry> applicableRanks = this.getApplicableRanks();
        return this.formatRankList(applicableRanks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rank rank = (Rank) o;
        return staff == rank.staff &&
                builder == rank.builder &&
                donor == rank.donor &&
                other == rank.other;
    }
}
