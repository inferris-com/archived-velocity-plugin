package com.inferris.util;

import com.inferris.server.Tags;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MessageUtil {
    public static void sendMessage(ProxiedPlayer player, BaseComponent message) {
        player.sendMessage(message);
    }

    public static void sendMessage(ProxiedPlayer player, String message){
        player.sendMessage(new TextComponent(message));
    }
    public static void sendMessage(ProxiedPlayer player, String message, boolean isStaff){
        player.sendMessage( new TextComponent(Tags.STAFF.getName(true) + message));
    }
}
