package com.inferris.events;

import com.inferris.Inferris;
import com.inferris.config.ConfigType;
import com.inferris.config.ConfigurationHandler;
import com.inferris.player.PlayerTaskManager;
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
import net.md_5.bungee.event.EventPriority;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EventJoin implements Listener {

    /**
     * This is responsible for any server switch events
     *
     * @param event Server switch event
     */

    @EventHandler
    public void onSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();
        PlayerTaskManager taskManager = new PlayerTaskManager(Inferris.getInstance().getProxy().getScheduler());
        sendHeader(player);

        PlayerDataManager playerDataManager = PlayerDataManager.getInstance();
        FriendsManager friendsManager = FriendsManager.getInstance();
        Friends friends = friendsManager.getFriendsData(player.getUniqueId());
        friendsManager.updateCache(player.getUniqueId(), friends);

            if (!ConfigurationHandler.getInstance().getProperties(ConfigType.PROPERTIES).getProperty("server.join.message").isEmpty()) {
                String joinMessageTemplate = ConfigurationHandler.getInstance().getProperties(ConfigType.PROPERTIES).getProperty("server.join.message");
                String joinMessage = joinMessageTemplate;
                if (joinMessageTemplate.contains("{version}")) {
                    joinMessage = joinMessageTemplate.replace("{version}", Inferris.getInstance().getDescription().getVersion());
                }

                player.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
                        joinMessage)));
            }

        // Important implementation
        if(playerDataManager.checkJoinedBefore(player)){
            Runnable task2 = () -> {
                player.sendMessage(TextComponent.fromLegacyText(ChatColor.YELLOW + generateRandomMessage(messageList())));
            };
            taskManager.addTaskForPlayer(task2, 3, TimeUnit.SECONDS);
        }

        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player, "onSwitch"); // Grabs the Redis cache
        Permissions.attachPermissions(player);

        playerData.setCurrentServer(ServerUtil.getServerType(player));

        playerDataManager.updateAllData(player, playerData); //new, so that it updates the bungee cache too;
    }

    /**
     * Responsible for single proxy connections
     *
     * @param event Post login event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        PlayerDataManager.getInstance().getPlayerDataAsync(player).thenAccept(playerData -> {
            Rank rank = playerData.getRank();
            RankRegistry rankRegistry = playerData.getByBranch();

            if (rank.getBranchID(Branch.STAFF) >= 1) {
                for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                    if (PlayerDataManager.getInstance().getPlayerData(player).getRank().getBranchID(Branch.STAFF) >= 1) {
                        proxiedPlayers.sendMessage(TextComponent.fromLegacyText(Tags.STAFF.getName(true) + rankRegistry.getPrefix(true) + rankRegistry.getColor() + player.getName() + ChatColor.YELLOW + " connected"));
                    }
                }
            }
        });
    }

    private void sendHeader(ProxiedPlayer player) {
        BaseComponent headerComponent = new TextComponent(ChatColor.AQUA + "Inferris");
        BaseComponent footerComponent = new TextComponent(Messages.WEBSITE_URL.getMessage());
        player.setTabHeader(headerComponent, footerComponent);
    }

    private static String generateRandomMessage(List<String> messages) {
        Random random = new Random();
        int index = random.nextInt(messages.size());
        return messages.get(index);
    }

    private List<String> messageList() {
        return List.of(
                "Welcome back! You're safe now.",
                "Your presence brightens our community every time.",
                "Hi! Remember, your presence makes a difference. Thanks for being here.",
                "Welcome back! We’ve missed you. It’s always better with you here.",
                "Welcome back! Your presence makes this place feel like home.",
                "Hey! You’re a vital part of our community. Thanks for being you!",
                "Welcome back! It’s always better with you around. Let’s make the most of today.",
                "Hi there! The moment you arrived, everything got better.",
                "Welcome back! Your return feels like sunshine after a rainy day.",
                "Hey! The room feels warmer and happier now that you’ve returned.\n"
        );
    }
}