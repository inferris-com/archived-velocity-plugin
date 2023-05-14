package com.inferris.events;

import com.inferris.Tags;
import com.inferris.channel.ChannelManager;
import com.inferris.channel.Channels;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventJoin implements Listener {

    @EventHandler
    public void onPostLogin(PostLoginEvent event){
        ProxiedPlayer player = event.getPlayer();
        sendHeader(player);

        RanksManager ranksManager = RanksManager.getInstance();
        PlayerDataManager playerDataManager = PlayerDataManager.getInstance();
        ChannelManager channelManager = ChannelManager.getInstance();

        Rank rank = ranksManager.getRank(player);
        ranksManager.cacheRank(player, rank);

        RankRegistry rankRegistry = playerDataManager.getPlayerData(player).getByBranch();

        Permissions.attachPermissions(player);
        channelManager.setChannel(player, Channels.NONE);

        if(rank.getBranchID(Branch.STAFF) >=1){
            for(ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()){
                if(ranksManager.getRank(proxiedPlayers).getBranchID(Branch.STAFF) >= 1){
                    proxiedPlayers.sendMessage(new TextComponent(Tags.STAFF.getText(true) + rankRegistry.getPrefix(true) + player.getName() + ChatColor.YELLOW + " connected"));
                }
            }
        }
    }

    private void sendHeader(ProxiedPlayer player){
        BaseComponent headerComponent = new TextComponent(ChatColor.AQUA + "Inferris");
        BaseComponent footerComponent = new TextComponent(ChatColor.GREEN + "https://inferris.com");
        player.setTabHeader(headerComponent, footerComponent);
    }
}
