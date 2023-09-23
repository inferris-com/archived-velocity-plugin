package com.inferris.events;

import com.inferris.Inferris;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;

public class EventPing implements Listener {

    @EventHandler
    public void onPing(ProxyPingEvent event){
        int count = ProxyServer.getInstance().getOnlineCount();

        ServerPing serverPing = event.getResponse();
        serverPing.setDescriptionComponent(new TextComponent(ChatColor.translateAlternateColorCodes('&', Inferris.getProperties().getProperty("server.list.motd"))));

        if(count >=1){
            int vanishedCount = getTotalVanishedPlayers();

            count = count - vanishedCount;

            serverPing.getPlayers().setOnline(vanishedCount);

            event.setResponse(serverPing);
        }
    }

    // TODO: Vanish count
    private int getTotalVanishedPlayers() {
        int count = 0;

        try (Jedis jedis = RegistryManager.getInstance().getJedisPool().getResource()) {

        }
        return 0;
    }
}
