package com.inferris.events;

import com.inferris.player.PlayerDataManager;
import com.inferris.util.Tags;
import com.inferris.rank.*;
import com.inferris.util.ConfigUtils;
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
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        sendHeader(player);
        ConfigUtils configUtils = new ConfigUtils();

        RanksManager ranksManager = RanksManager.getInstance();
        PlayerDataManager playerDataManager = PlayerDataManager.getInstance();

        playerDataManager.checkJoinedBefore(player); // Important implementation

        //Rank rank = ranksManager.getRank(player);
        //ranksManager.cacheRank(player, rank);

        //RankRegistry rankRegistry = playerDataManager.getPlayerData(player).getByBranch();

        //Permissions.attachPermissions(player);

//        if (rank.getBranchID(Branch.STAFF) >= 1) {
//            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
//                if (ranksManager.getRank(proxiedPlayers).getBranchID(Branch.STAFF) >= 1) {
//                    proxiedPlayers.sendMessage(new TextComponent(Tags.STAFF.getName(true) + rankRegistry.getPrefix(true) + player.getName() + ChatColor.YELLOW + " connected"));
//                }
//            }
//        }
//
//        playerDataManager.checkJoinedBefore(player);
    }

    private void sendHeader(ProxiedPlayer player) {
        BaseComponent headerComponent = new TextComponent(ChatColor.AQUA + "Inferris");
        BaseComponent footerComponent = new TextComponent(ChatColor.GREEN + "https://inferris.com");
        player.setTabHeader(headerComponent, footerComponent);
    }
}
