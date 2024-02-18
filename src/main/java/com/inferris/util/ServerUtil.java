package com.inferris.util;

import com.inferris.Inferris;
import com.inferris.server.Server;
import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.logging.Level;

public class ServerUtil {

    public static void broadcastMessage(String message) {
        for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
            all.sendMessage(new TextComponent(message));
        }
    }

    // todo please fix this if possible
    public static Server getServerType(ProxiedPlayer player) {
        switch (player.getServer().getAddress().getPort()) {
            case 25566 -> {
                return Server.LOBBY;
            }
            case 25567 -> {
                return Server.INFERRIS;
            }
            default -> {
                return Server.UNKNOWN;
            }
        }
    }

    public static void log(String message, Level level){
        Inferris.getInstance().getLogger().log(level, message);
    }

    public static void log(String message, Level level, ServerState serverState) {
        if(ServerStateManager.getCurrentState() == serverState){
            log(message, level);
        }
    }

    public static void log(String message, Level level, ServerState serverState, ServerState serverState2) {
        if(ServerStateManager.getCurrentState() == serverState || ServerStateManager.getCurrentState() == serverState2){
            log(message, level);
        }
    }
}
