package com.inferris.rank;

import net.md_5.bungee.api.ChatColor;

public enum RankRegistry {
    ADMIN(ChatColor.RED + "[Admin]", Branch.STAFF, 3),
    MOD(ChatColor.DARK_GREEN + "[Mod]", Branch.STAFF, 2),
    HELPER(ChatColor.BLUE + "[Helper]", Branch.STAFF, 1),
    DONOR(ChatColor.AQUA + "[Backer]", Branch.DONOR, 1),
    NONE(String.valueOf(ChatColor.RESET), null, 0);

    private final String prefix;
    private final Branch branch;
    private final int level;

    RankRegistry(String prefix, Branch branch, int level) {
        this.prefix = prefix;
        this.branch = branch;
        this.level = level;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPrefix(boolean withSpace) {
        return switch (this) {
            case ADMIN, MOD, HELPER, DONOR -> prefix + " ";
            default -> "";
        };
    }

    public Branch getBranch() {
        return branch;
    }

    public int getLevel() {
        return level;
    }
}