package com.inferris.commands;

import com.inferris.server.Messages;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.friends.Friends;
import com.inferris.player.friends.FriendsManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;

public class CommandFriend extends Command implements TabExecutor {
    public CommandFriend(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if (length == 0) {
                player.sendMessage(new TextComponent(ChatColor.YELLOW + "/friend add <player>"));
                player.sendMessage(new TextComponent(ChatColor.YELLOW + "/friend accept <player>"));
                player.sendMessage(new TextComponent(ChatColor.YELLOW + "/friend remove <player>"));
                return;
            }
            if (length == 1) {
                String subCommand = args[0];
                if (subCommand.equalsIgnoreCase("remove")) {
                    player.sendMessage(new TextComponent(ChatColor.YELLOW + "/friend remove <player>"));
                    player.sendMessage(new TextComponent(ChatColor.GREEN + "Don't worry, we value privacy and we won't inform the player about the removal."));
                    return;
                }
                if (subCommand.equalsIgnoreCase("add")) {
                    player.sendMessage(new TextComponent(ChatColor.YELLOW + "/friend add <player>"));
                    return;
                }
                if (subCommand.equalsIgnoreCase("accept")) {
                    player.sendMessage(new TextComponent(ChatColor.YELLOW + "/friend accept <player>"));
                    return;
                }
                if (subCommand.equalsIgnoreCase("list")) {
                    UUID playerUUID = player.getUniqueId();
                    FriendsManager.getInstance().listFriends(playerUUID, 1);
                    return;
                }
                player.sendMessage(new TextComponent(ChatColor.YELLOW + "Unknown command argument: " + subCommand));
            }
            if (length == 2) {
                FriendsManager friendsManager = FriendsManager.getInstance();
                UUID playerUUID = player.getUniqueId();

                if (args[0].equalsIgnoreCase("list")) {
                    int index = Integer.parseInt(args[1]);
                    friendsManager.listFriends(player.getUniqueId(), index);
                    return;
                }
                UUID targetUUID = PlayerDataManager.getInstance().getUUIDByUsername(args[1]);
                if (targetUUID == null) {
                    player.sendMessage(new TextComponent(Messages.PLAYER_NOT_IN_SYSTEM.getMessage()));
                    return;
                }
                PlayerData targetData = PlayerDataManager.getInstance().getRedisData(targetUUID);
                String targetName = targetData.getUsername();
                Friends playerFriends = FriendsManager.getInstance().getFriendsData(playerUUID);
                Friends targetFriends = FriendsManager.getInstance().getFriendsData(targetUUID);

                if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("request")) {
                    friendsManager.friendRequest(playerUUID, targetUUID);

                } else if (args[0].equalsIgnoreCase("accept")) {
                    if (targetFriends.getPendingFriendsList().contains(playerUUID)) {
                        friendsManager.addFriend(playerUUID, targetUUID);

                        player.sendMessage(new TextComponent(ChatColor.GREEN + "You are now friends with " + targetName));

                        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetUUID);
                        if (target != null)
                            target.sendMessage(new TextComponent(ChatColor.GREEN + "You are now friends with " + player.getName()));
                    } else {
                        player.sendMessage(new TextComponent(ChatColor.RED + "You don't have a pending friend request from " + targetName));
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    FriendsManager.getInstance().removeFriend(playerUUID, targetUUID);
                } else if (args[0].equalsIgnoreCase("reject") || args[0].equalsIgnoreCase("deny")) {
                    FriendsManager.getInstance().rejectFriendRequest(playerUUID, targetUUID);
                }
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        int length = args.length;
        if (sender instanceof ProxiedPlayer player) {
            if (length < 2) {
                String partialOption = args[0].toLowerCase();
                List<String> options = new ArrayList<>();
                List<String> availableOptions = Arrays.asList("add", "accept", "list", "remove", "deny");
                for (String option : availableOptions) {
                    if (option.toLowerCase().startsWith(partialOption)) {
                        options.add(option);
                    }
                }
                return options;
            }
            if (length == 2) {
                String partialPlayerName = args[0];
                List<String> playerNames = new ArrayList<>();
                PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
                for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                    if (PlayerDataManager.getInstance().getPlayerData(proxiedPlayers).getVanishState() == VanishState.DISABLED || playerData.getBranchValue(Branch.STAFF) >= 3) {
                        String playerName = proxiedPlayers.getName();
                        if (playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())) {
                            playerNames.add(playerName);
                        }
                    }
                }
                return playerNames;
            }
        }
        return Collections.emptyList();
    }
}
