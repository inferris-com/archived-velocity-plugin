package com.inferris.rank;

import net.md_5.bungee.api.ChatColor;

public enum RankRegistry {
    ADMIN(ChatColor.RED + "[Admin]", Branch.STAFF, 3, ChatColor.RED),
    MOD(ChatColor.DARK_GREEN + "[Mod]", Branch.STAFF, 2, ChatColor.DARK_GREEN),
    HELPER(ChatColor.BLUE + "[Helper]", Branch.STAFF, 1, ChatColor.BLUE),
    DONOR(ChatColor.AQUA + "[Backer]", Branch.DONOR, 1, ChatColor.AQUA),
    NONE(String.valueOf(ChatColor.RESET), null, 0, ChatColor.RESET);

    private final String prefix;
    private final Branch branch;
    private final int level;
    private final ChatColor color;

    RankRegistry(String prefix, Branch branch, int level, ChatColor color) {
        this.prefix = prefix;
        this.branch = branch;
        this.level = level;
        this.color = color;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getPrefix(boolean withSpace) {
        if (withSpace) {
            switch (this) {
                case ADMIN, MOD, HELPER, DONOR -> {
                    return prefix + " ";
                }
                default -> {
                    return prefix;
                }
            }
        } else {
            return prefix;
        }
    }

    public Branch getBranch() {
        return branch;
    }

    public int getLevel() {
        return level;
    }

    public ChatColor getColor() {
        return color;
    }
}