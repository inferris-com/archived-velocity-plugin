package com.inferris.rank;

import com.inferris.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;

public enum RankRegistry {
    ADMIN(ChatUtil.translateToHex("&8⎰" + "#FF5733Admin" + "&8⎱"), Branch.STAFF, 3, ChatColor.RESET),
    MOD(ChatColor.DARK_GREEN + "[Mod]", Branch.STAFF, 2, ChatColor.DARK_GREEN),
    HELPER(ChatColor.BLUE + "[Helper]", Branch.STAFF, 1, ChatColor.BLUE),
    BUILDER(ChatColor.GOLD + "[Builder]", Branch.BUILDER, 1, ChatColor.GOLD),
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
                case ADMIN, MOD, HELPER, BUILDER, DONOR -> {
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