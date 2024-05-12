package com.inferris.events;

import com.inferris.Inferris;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.vanish.VanishState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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

            count = count - getTotalVanishedPlayers();

            serverPing.getPlayers().setOnline(count);

            event.setResponse(serverPing);
        }
    }

    private int getTotalVanishedPlayers() {
        int vanishedCount = 0;
        for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()){
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
            if(playerData.getVanishState() == VanishState.ENABLED){
                vanishedCount++;
            }
        }
        return vanishedCount;
    }
}
