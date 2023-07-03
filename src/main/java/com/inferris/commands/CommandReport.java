package com.inferris.commands;

import com.inferris.Inferris;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.server.ReportPayload;
import com.inferris.util.Tags;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;

public class CommandReport extends Command implements TabExecutor {
    public CommandReport(String name) {
        super(name);
    }

    List<String> possibleReasons = List.of("spamming", "harassment", "inappropriate_behavior", "cheating", "exploiting_bugs", "impersonation", "scamming", "advertisement", "other");

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if (length == 0) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /report <player>"));

            } else if (length == 1) {
                StringBuilder reasonsMessage = new StringBuilder();

                for (int i = 0; i < possibleReasons.size(); i++) {
                    if (i > 0) {
                        reasonsMessage.append(ChatColor.GRAY).append(", ");
                    }

                    String reason = possibleReasons.get(i);
                    reasonsMessage.append(ChatColor.RESET).append(ChatColor.WHITE).append(reason.substring(0, 1).toUpperCase()).append(reason.substring(1));
                }

                player.sendMessage(new TextComponent(ChatColor.RED + "Please provide a reason for the report."));
                player.sendMessage(new TextComponent(ChatColor.YELLOW + "Available reasons: " + reasonsMessage));

            } else if (length == 2) {
                Inferris.getInstance().getLogger().warning("Cmd issued");
                String reason = args[1].toLowerCase();
                if (!possibleReasons.contains(reason)) {
                    player.sendMessage(new TextComponent(ChatColor.RED + "Invalid reason: " + reason));
                    return;
                }

                UUID uuid = PlayerDataManager.getInstance().getUUIDByUsername(args[0]);
                if (uuid == null) {
                    player.sendMessage(new TextComponent(ChatColor.RED + "That player does not exist."));
                    return;
                }

                PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(player.getUniqueId());
                PlayerData targetPlayerData = PlayerDataManager.getInstance().getRedisDataOrNull(uuid);
                ReportPayload reportPayload = new ReportPayload(playerData.getNameColor() + player.getName(), targetPlayerData.getRegistry().getUsername(), args[1], player.getServer().getInfo().getName());

                for (ProxiedPlayer staffPlayer : ProxyServer.getInstance().getPlayers()) {
                    // Check if the player is a staff member
                    if (PlayerDataManager.getInstance().getPlayerData(staffPlayer).isStaff()) {
                        staffPlayer.sendMessage(new TextComponent(Tags.STAFF.getName(true) + ChatColor.RED + "New chat report!"));
                        staffPlayer.sendMessage(new TextComponent(""));

                        // Send the report information to the staff player
                        staffPlayer.sendMessage(new TextComponent(ChatColor.GRAY + "Reported Player: " + ChatColor.YELLOW + reportPayload.getReported()));
                        staffPlayer.sendMessage(new TextComponent(ChatColor.GRAY + "Reported by: " + ChatColor.RESET + reportPayload.getSender()));
                        staffPlayer.sendMessage(new TextComponent(""));

                        staffPlayer.sendMessage(new TextComponent(ChatColor.GRAY + "Reason: " + ChatColor.YELLOW + reportPayload.getReason()));
                        staffPlayer.sendMessage(new TextComponent(ChatColor.GRAY + "Server: " + ChatColor.GOLD + reportPayload.getServer()));
                    }
                }
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        int length = args.length;
        if (sender instanceof ProxiedPlayer player) {
            if (length == 1) {
                String partialPlayerName = args[0];
                List<String> playerNames = new ArrayList<>();
                for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                    if (!(PlayerDataManager.getInstance().getPlayerData(proxiedPlayers).getVanishState() == VanishState.ENABLED)) {
                        String playerName = proxiedPlayers.getName();
                        if (playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())) {
                            playerNames.add(playerName);
                        }
                    }
                }
                return playerNames;
            }
            if (length == 2) {
                String partialOption = args[1].toLowerCase();
                List<String> options = new ArrayList<>();

                for (String option : possibleReasons) {
                    if (option.toLowerCase().startsWith(partialOption)) {
                        options.add(option);
                    }
                }
                return options;
            }
        }
        return Collections.emptyList();
    }
}
