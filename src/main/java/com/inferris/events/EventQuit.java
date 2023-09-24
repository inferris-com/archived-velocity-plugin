package com.inferris.events;

import com.inferris.player.PlayerDataManager;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventQuit implements Listener {

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
//        ProxiedPlayer player = event.getPlayer();
//        Rank rank = RanksManager.getInstance().getRank(player);
//        RanksManager ranksManager = RanksManager.getInstance();
//        //RankRegistry rankRegistry = PlayerDataManager.getInstance().getPlayerData(player).getByBranch(); todo
//
//        if(rank.getBranchID(Branch.STAFF) >=1){
//            for(ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()){
//                if(ranksManager.getRank(proxiedPlayers).getBranchID(Branch.STAFF) >= 1){
//                    proxiedPlayers.sendMessage(new TextComponent(Tags.STAFF.getName(true) + rankRegistry.getPrefix(true) + player.getName() + ChatColor.YELLOW + " disconnected"));
//                }
//            }
//        }
//
//        RanksManager.getInstance().invalidate(player);
//        PlayerDataManager.getInstance().invalidatePlayerData(player); //todo
//    }

        PlayerDataManager.getInstance().invalidateCache(event.getPlayer());
        //PlayerDataManager.getInstance().invalidateRedisEntry(event.getPlayer());
    }
}
