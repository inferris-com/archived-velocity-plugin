package com.inferris.events;

import com.inferris.Inferris;
import com.inferris.common.ColorType;
import com.inferris.config.ConfigType;
import com.inferris.config.ConfigurationHandler;
import com.inferris.player.PlayerData;
import com.inferris.player.service.PlayerDataManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.server.PlayerCountManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventPing implements Listener {

    @EventHandler
    public void onPing(ProxyPingEvent event){
        int count = ProxyServer.getInstance().getOnlineCount();

        ServerPing serverPing = event.getResponse();
        String motdTemplate = ConfigurationHandler.getInstance().getProperties(ConfigType.PROPERTIES).getProperty("server.list.motd");
        String motdMessage = motdTemplate;
        if (motdTemplate.contains("Inferris")) {
            motdMessage = motdTemplate.replace("Inferris", ChatColor.of(ColorType.BRAND_SECONDARY.getColor()) + "Inferris");
        }

        BaseComponent[] components = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', motdMessage));
        TextComponent descriptionComponent = new TextComponent();

        for (BaseComponent component : components) {
            descriptionComponent.addExtra(component);
        }

        serverPing.setDescriptionComponent(descriptionComponent);

        if(PlayerCountManager.isOverridden()){
            count = PlayerCountManager.getOverriddenCount();
        } else {
            if(count >= 1){
                count = count - Inferris.getInstance().getTotalVanishedPlayers();
            }
        }

        serverPing.getPlayers().setOnline(count);
        event.setResponse(serverPing);
    }
}