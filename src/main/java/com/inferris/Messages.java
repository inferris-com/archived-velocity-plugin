package com.inferris;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public enum Messages {
    COMMAND_VANISH_ENABLED(new TextComponent(ChatColor.GREEN + "Vanish has been enabled")),
    COMMAND_VANISH_DISABLED(new TextComponent(ChatColor.RED + "Vanish has been disabled")),
    NO_PERMISSION(new TextComponent(ChatColor.RED + "You do not have permission to use this command")),
    WEBSITE_URL(new TextComponent(ChatColor.GREEN + "https://inferris.com"));

    private final TextComponent message;
    Messages(TextComponent message){
        this.message = message;
    }

    public TextComponent getMessage() {
        return message;
    }
}
