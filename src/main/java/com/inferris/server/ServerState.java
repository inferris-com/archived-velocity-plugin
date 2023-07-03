package com.inferris.server;

import net.md_5.bungee.api.ChatColor;

public enum ServerState {
    NORMAL(ChatColor.GREEN),
    DEV(ChatColor.RED),
    DEBUG(ChatColor.RED);

    private final ChatColor color;
    ServerState(ChatColor color){
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }
}