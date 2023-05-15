package com.inferris.events;

import com.inferris.Inferris;
import com.inferris.channel.Tags;
import com.inferris.channel.ChannelManager;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RankRegistry;
import com.inferris.rank.RanksManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventQuit implements Listener {

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event){
        ProxiedPlayer player = event.getPlayer();
        Rank rank = RanksManager.getInstance().getRank(player);
        RanksManager ranksManager = RanksManager.getInstance();
        ChannelManager channelManager = ChannelManager.getInstance();
        RankRegistry rankRegistry = PlayerDataManager.getInstance().getPlayerData(player).getByBranch();

        if(rank.getBranchID(Branch.STAFF) >=1){
            for(ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()){
                if(ranksManager.getRank(proxiedPlayers).getBranchID(Branch.STAFF) >= 1){
                    proxiedPlayers.sendMessage(new TextComponent(Tags.STAFF.getText(true) + rankRegistry.getPrefix(true) + player.getName() + ChatColor.YELLOW + " disconnected"));
                }
            }
        }

        RanksManager.getInstance().invalidate(player);
        PlayerDataManager.getInstance().invalidatePlayerData(player);
        channelManager.invalidate(player);
    }
}
