package com.inferris.events;

import com.inferris.Inferris;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.registry.Registry;
import com.inferris.player.registry.RegistryManager;
import com.inferris.player.vanish.VanishState;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

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
        for (Registry registry : RegistryManager.getPlayerRegistryCache().asMap().values()) {
            if (registry.getVanishState() == VanishState.ENABLED) {
                count++;
            }
        }
        return count;
    }
}
