package com.inferris.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.player.PlayerData;
import com.inferris.player.registry.Registry;
import com.inferris.player.registry.RegistryManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.util.CacheSerializationUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class EventPing implements Listener {

    @EventHandler
    public void onPing(ProxyPingEvent event){
        int count = ProxyServer.getInstance().getOnlineCount();

        if(count >=1){
            int vanishedCount = getTotalVanishedPlayers();

            count = count - vanishedCount;
            ServerPing ping = event.getResponse();

            ping.getPlayers().setOnline(vanishedCount);

            event.setResponse(ping);
        }
    }

    private int getTotalVanishedPlayers() {
        int count = 0;

        try (Jedis jedis = RegistryManager.getInstance().getJedisPool().getResource()) {

        }
        return 0;
    }
}
