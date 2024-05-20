package com.inferris.server;

import net.md_5.bungee.api.ChatColor;

public enum Tag {
    STAFF(ChatColor.AQUA + "[STAFF]");

    private final String name;
    Tag(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getName(boolean withSpace) {
        if (withSpace) {
            return name + ChatColor.RESET + " ";
        }
        return getName();
    }
}
