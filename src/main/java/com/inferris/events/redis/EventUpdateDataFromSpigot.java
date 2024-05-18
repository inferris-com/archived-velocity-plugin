package com.inferris.events.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.events.redis.dispatching.JedisEventHandler;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.util.SerializationUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class EventUpdateDataFromSpigot implements JedisEventHandler {
    @Override
    public void handle(String message) {
        Inferris.getInstance().getLogger().severe("Spigot updated Proxy cache");

        try {
            PlayerData playerData = SerializationUtils.deserializePlayerData(message);
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerData.getUuid());
            PlayerDataManager.getInstance().updateAllData(player, playerData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
