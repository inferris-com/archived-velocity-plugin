package com.inferris;

import net.md_5.bungee.api.ChatColor;

public enum Tags {
    STAFF(ChatColor.AQUA + "[STAFF]");

    private final String text;

    Tags(String text){
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getText(boolean withSpace) {
        return text + ChatColor.RESET + " ";
    }
}
