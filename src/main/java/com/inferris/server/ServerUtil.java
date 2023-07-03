package com.inferris.server;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServerUtil {

    public static void broadcastMessage(String message){
        for(ProxiedPlayer all : ProxyServer.getInstance().getPlayers()){
            all.sendMessage(new TextComponent(message));
        }
    }
}
