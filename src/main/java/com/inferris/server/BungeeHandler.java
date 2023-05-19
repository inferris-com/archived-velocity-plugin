package com.inferris.server;

import com.inferris.Inferris;
import com.inferris.util.BungeeUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeHandler {

    public void requestData(ProxiedPlayer player, BungeeChannel types) {
        switch (types) {
            case PLAYER_REGISTRY -> {
                BungeeUtils.sendBungeeMessage(player, BungeeChannel.PLAYER_REGISTRY, Subchannel.REQUEST);
                Inferris.getInstance().getLogger().warning("Requested player registry");
            }
        }
    }

    public void sendData(ProxiedPlayer player, BungeeChannel types, String message){
        switch (types){
            case STAFFCHAT -> {
                BungeeUtils.sendBungeeMessage(player, BungeeChannel.STAFFCHAT, Subchannel.FORWARD, message);
                Inferris.getInstance().getLogger().warning("Sent staffchat");
            }
        }
    }
}