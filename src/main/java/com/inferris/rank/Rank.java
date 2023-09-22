package com.inferris.rank;

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

    public int getBranchID(Branch branch) {
        return switch (branch) {
            case STAFF -> staff;
            case BUILDER -> builder;
            case DONOR -> donor;
            case OTHER -> other;
            default -> -1;
        };
    }
}
