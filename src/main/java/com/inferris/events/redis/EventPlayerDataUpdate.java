package com.inferris.events.redis;

import com.inferris.Inferris;
import com.inferris.events.redis.dispatching.JedisEventHandler;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.manager.ManagerContainer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class EventPlayerDataUpdate implements JedisEventHandler {
    @Override
    public void handle(PlayerDataService playerDataService, ManagerContainer managerContainer, String message, String senderId) {
        //todo handle offline
        Inferris.getInstance().getLogger().severe("Proxy received update (EventPlayerDataUpdate)");
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(message));
        playerDataService.updateLocalPlayerData(player.getUniqueId(), playerData -> {});
    }
}
