package com.inferris.player;

import net.md_5.bungee.api.ChatColor;

public enum Channel {
    STAFF(ChatColor.AQUA + "STAFF"),
    SPECIAL("SPECIAL"),
    NONE("NONE");

    private final String message;
    private final String lowercaseName;
    Channel(String message){
        this.message = message;
        this.lowercaseName = name().toLowerCase();
    }

    public String getMessage() {
        return message;
    }

    public String getLowercaseName() {
        return lowercaseName;
    }
}
