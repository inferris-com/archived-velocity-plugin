package com.inferris.events.redis;

import com.inferris.Inferris;
import com.inferris.events.redis.dispatching.JedisEventHandler;
import com.inferris.player.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class EventPlayerDataUpdate implements JedisEventHandler {
    @Override
    public void handle(String message, String senderId) {
        //todo handle offline
        PlayerDataService playerDataService = ServiceLocator.getPlayerDataService();
        Inferris.getInstance().getLogger().severe("Proxy received update");
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(message));
        playerDataService.updatePlayerDataWithoutPush(player.getUniqueId(), playerData -> {});
    }
}
