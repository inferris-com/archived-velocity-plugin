package com.inferris.util;

import net.md_5.bungee.api.ChatColor;

public enum Tags {
    STAFF(ChatColor.AQUA + "[STAFF]");

    private final String name;
    Tags(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getName(boolean withSpace){
        return name + ChatColor.RESET + " ";
    }
}
