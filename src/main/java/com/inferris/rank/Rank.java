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

    public Rank(){
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

    public void setStaff(int staff) {
        this.staff = staff;
    }

    public void setDonor(int donor) {
        this.donor = donor;
    }

    public void setOther(int other) {
        this.other = other;
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
