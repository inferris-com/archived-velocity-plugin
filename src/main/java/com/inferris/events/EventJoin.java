package com.inferris.events;

import com.inferris.server.Messages;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.friends.Friends;
import com.inferris.player.friends.FriendsManager;
import com.inferris.rank.*;
import com.inferris.util.ServerUtil;
import com.inferris.server.Tags;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventJoin implements Listener {

    /**
     * This is responsible for any server switch events
     *
     * @param event Server switch event
     */

    @EventHandler
    public void onSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        sendHeader(player);

        PlayerDataManager playerDataManager = PlayerDataManager.getInstance();
        FriendsManager friendsManager = FriendsManager.getInstance();
        Friends friends = friendsManager.getFriendsData(player.getUniqueId());
        friendsManager.updateCache(player.getUniqueId(), friends);

        // Important implementation
        playerDataManager.checkJoinedBefore(player);

        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player, "onSwitch"); // Grabs the Redis cache
        Permissions.attachPermissions(player);

        playerData.setCurrentServer(ServerUtil.getServerType(player));

        playerDataManager.updateAllData(player, playerData); //new, so that it updates the bungee cache too
    }

    /**
     * Responsible for single proxy connections
     *
     * @param event Post login event
     */
// Todo, enable event
    @EventHandler
    public void onPostLogin(PostLoginEvent event) {

        ProxiedPlayer player = event.getPlayer();

        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player, "onPostLogin");

        Rank rank = playerData.getRank();
        RankRegistry rankRegistry = playerData.getByBranch();

        if (rank.getBranchID(Branch.STAFF) >= 1) {
            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                if (playerData.getRank().getBranchID(Branch.STAFF) >= 1) {
                    proxiedPlayers.sendMessage(TextComponent.fromLegacyText(Tags.STAFF.getName(true) + rankRegistry.getPrefix(true) + rankRegistry.getColor() + player.getName() + ChatColor.YELLOW + " connected"));
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