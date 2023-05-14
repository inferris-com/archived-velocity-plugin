package com.inferris.rank;

public class Rank {
    private int staff;
    private int donor;
    private int other;

    public Rank(int staff, int donor, int other) {
        this.staff = staff;
        this.donor = donor;
        this.other = other;
    }

    public int getStaff() {
        return staff;
    }

    public int getDonor() {
        return donor;
    }

    public int getOther() {
        return other;
    }

    public int getBranchID(Branch branch) {
        switch (branch) {
            case STAFF:
                return staff;
            case DONOR:
                return donor;
            case OTHER:
                return other;
            default:
                return -1;
        }
    }
}
