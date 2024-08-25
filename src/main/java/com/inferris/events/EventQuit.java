package com.inferris.events;

import com.google.inject.Inject;
import com.inferris.player.PlayerData;
import com.inferris.player.manager.ManagerContainer;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RankRegistry;
import com.inferris.server.PlayerSessionManager;
import com.inferris.server.Tag;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventQuit implements Listener {

    private final PlayerDataService playerDataService;
    private final ManagerContainer managerContainer;

    @Inject
    public EventQuit(PlayerDataService playerDataService, ManagerContainer managerContainer){
        this.playerDataService = playerDataService;
        this.managerContainer = managerContainer;
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        PlayerData playerData = playerDataService.getPlayerData(player.getUniqueId());

        if(playerData == null){
            return;
        }

        RankRegistry rankRegistry = playerData.getRank().getByBranch();
        Rank rank = playerData.getRank();

        if (rank.getBranchID(Branch.STAFF) >= 1) {
            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                PlayerData proxiedPlayerData = playerDataService.getPlayerData(proxiedPlayers.getUniqueId());
                if (proxiedPlayerData.getRank().getBranchID(Branch.STAFF) >= 1) {
                    proxiedPlayers.sendMessage(TextComponent.fromLegacyText(Tag.STAFF.getName(true) + rankRegistry.getPrefix(true) + rankRegistry.getColor() + player.getName() + ChatColor.YELLOW + " disconnected"));
                }
            }
        }
        managerContainer.getPlayerDataManager().getCache().invalidate(player.getUniqueId());
        PlayerSessionManager.clearPlayerSession(player.getUniqueId());
    }
}
