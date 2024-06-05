package com.inferris.player;

import com.inferris.rank.Branch;
import net.md_5.bungee.api.ChatColor;

public enum Channel {
    STAFF(Branch.STAFF, 1, ChatColor.AQUA + "[STAFF]"),
    ADMIN(Branch.STAFF, 3, ChatColor.RED + "[ADMIN]"),
    SPECIAL(Branch.OTHER, 1, "SPECIAL"),
    NONE(null, 0, "NONE");

    private final Branch branch;
    private final int minimumId;
    private final String tag;
    private final String lowercaseName;
    Channel(Branch branch, int minimumId, String message){
        this.branch = branch;
        this.minimumId = minimumId;
        this.tag = message;
        this.lowercaseName = name().toLowerCase();
    }

    public Branch getBranch() {
        return branch;
    }

    public int getMinimumId() {
        return minimumId;
    }

    public String getTag() {
        return tag;
    }

    public String getTag(boolean hasSpacer) {
        if (hasSpacer) {
            return tag + ChatColor.RESET + " ";
        }
        return tag;
    }

    public String getLowercaseName() {
        return lowercaseName;
    }
}
