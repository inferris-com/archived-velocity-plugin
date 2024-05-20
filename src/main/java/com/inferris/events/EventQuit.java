package com.inferris.events;

import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RankRegistry;
import com.inferris.server.Tag;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventQuit implements Listener {

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
        RankRegistry rankRegistry = playerData.getByBranch();
        Rank rank = playerData.getRank();

        if (rank.getBranchID(Branch.STAFF) >= 1) {
            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                PlayerData proxiedPlayerData = PlayerDataManager.getInstance().getPlayerData(proxiedPlayers);
                if (proxiedPlayerData.getRank().getBranchID(Branch.STAFF) >= 1) {
                    proxiedPlayers.sendMessage(TextComponent.fromLegacyText(Tag.STAFF.getName(true) + rankRegistry.getPrefix(true) + rankRegistry.getColor() + player.getName() + ChatColor.YELLOW + " disconnected"));
                }
            }
        }
        PlayerDataManager.getInstance().invalidateCache(player);
    }
}
