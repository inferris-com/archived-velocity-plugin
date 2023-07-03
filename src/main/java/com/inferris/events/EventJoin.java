package com.inferris.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.Messages;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.server.JedisChannels;
import com.inferris.server.Ports;
import com.inferris.util.CacheSerializationUtils;
import com.inferris.util.Tags;
import com.inferris.rank.*;
import com.inferris.util.ConfigUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import redis.clients.jedis.Jedis;

public class EventJoin implements Listener {

    /**
     * This is responsible for any server switch events
     * @param event
     */

    @EventHandler
    public void onSwitch(ServerSwitchEvent event){
        ProxiedPlayer player = event.getPlayer();
        sendHeader(player);
        ConfigUtils configUtils = new ConfigUtils();

        RanksManager ranksManager = RanksManager.getInstance();
        PlayerDataManager playerDataManager = PlayerDataManager.getInstance();

        playerDataManager.checkJoinedBefore(player); // Important implementation
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
        Rank rank = playerData.getRank();
        RankRegistry rankRegistry = playerDataManager.getPlayerData(player).getByBranch();

        Permissions.attachPermissions(player);

        try(Jedis jedis = Inferris.getJedisPool().getResource()){
            String json = CacheSerializationUtils.serializePlayerData(playerDataManager.getPlayerData(player));
            player.sendMessage(new TextComponent("Bungee " + json));
            Inferris.getInstance().getLogger().info(json);
            jedis.publish(JedisChannels.PLAYERDATA_JOIN.name(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Responsible for single proxy connections
     * @param event
     */

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
        PlayerDataManager playerDataManager = PlayerDataManager.getInstance();

        Rank rank = playerData.getRank();
        RankRegistry rankRegistry = playerDataManager.getPlayerData(player).getByBranch();

        if (rank.getBranchID(Branch.STAFF) >= 1) {
            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                if (playerDataManager.getPlayerData(proxiedPlayers).getRank().getBranchID(Branch.STAFF) >= 1) {
                    proxiedPlayers.sendMessage(new TextComponent(Tags.STAFF.getName(true) + rankRegistry.getPrefix(true) + player.getName() + ChatColor.YELLOW + " connected"));
                }
            }
        }
    }

    private void sendHeader(ProxiedPlayer player) {
        BaseComponent headerComponent = new TextComponent(ChatColor.AQUA + "Inferris");
        BaseComponent footerComponent = new TextComponent(Messages.WEBSITE_URL.getMessage());
        player.setTabHeader(headerComponent, footerComponent);
    }
}
