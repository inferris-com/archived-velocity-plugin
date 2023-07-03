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
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;

/**
 * <p>The CommandReport class represents a command used to report players in a game server.
 * It allows players to report other players for various reasons and notifies staff members of new reports.
 * This command can be executed by players who want to report another player's misconduct or inappropriate behavior.
 * The reported player's username and the reason for the report are collected as command arguments.
 * The command then notifies online staff members about the new report, providing them with relevant information.</p>
 * <p>Staff members receive a notification with details about the reported player, including their username, the reason for the report,
 * the server they are playing on, and the player who initiated the report. The staff members can click on various elements
 * within the notification to view additional information or perform actions related to the report.
 * The CommandReport class also provides tab completion functionality, allowing players to autocomplete player names and
 * available report reasons as they type the command.</p>
 *
 * @since 1.0
 */

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

                String stringBuilder = reason.substring(0, 1).toUpperCase() + reason.substring(1);

                PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(player.getUniqueId());
                PlayerData targetPlayerData = PlayerDataManager.getInstance().getRedisDataOrNull(uuid);
                String username = targetPlayerData.getRegistry().getUsername();

                ReportPayload reportPayload = new ReportPayload(
                        playerData.getByBranch().getPrefix(true) + playerData.getNameColor() + player.getName(),
                        targetPlayerData.getRegistry().getUsername(), stringBuilder,
                        player.getServer().getInfo().getName());

                TextComponent reportedPlayer = new TextComponent(ChatColor.GRAY + "Reported: " + ChatColor.YELLOW + reportPayload.getReported());
                TextComponent info = new TextComponent(ChatColor.GREEN + "[Infractions]");
                info.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Click to view info")));
                info.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/history " + username));

                TextComponent accountInfo = new TextComponent(ChatColor.GREEN + "[Account]");
                accountInfo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.GREEN + "Click to view account info")));
                accountInfo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/account " + username));

                reportedPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to copy username")));
                reportedPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, username));

                TextComponent server = new TextComponent(ChatColor.GRAY + "Server: " + ChatColor.GOLD + reportPayload.getServer());
                server.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.AQUA + "Click to join server")));
                server.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server lobby"));
                //server.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/server lobby"));

                TextComponent senderPlayer = new TextComponent(ChatColor.GRAY + "Reported by: " + ChatColor.RESET + reportPayload.getSender());
                senderPlayer.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to copy username")));
                senderPlayer.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, playerData.getRegistry().getUsername()));

                TextComponent logs = new TextComponent(ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/viewlogs " + reportPayload.getServer() + ChatColor.GRAY + " or click here");
                logs.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.AQUA + "Click to run command")));
                logs.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewlogs " + reportPayload.getServer()));

                TextComponent spacer = new TextComponent("");


                for (ProxiedPlayer staffPlayer : ProxyServer.getInstance().getPlayers()) {
                    // Check if the player is a staff member
                    if (PlayerDataManager.getInstance().getPlayerData(staffPlayer).isStaff()) {
                        staffPlayer.sendMessage(new TextComponent(Tags.STAFF.getName(true) + ChatColor.RED + "New chat report!"));
                        staffPlayer.sendMessage(new TextComponent(""));

                        // Send the report information to the staff player
                        staffPlayer.sendMessage(reportedPlayer, new TextComponent(" "), info, new TextComponent(" "), accountInfo);
                        staffPlayer.sendMessage(new TextComponent(ChatColor.GRAY + "Reason: " + ChatColor.RESET + reportPayload.getReason()));
                        staffPlayer.sendMessage(spacer);
                        staffPlayer.sendMessage(server);
                        staffPlayer.sendMessage(senderPlayer);
                        staffPlayer.sendMessage(spacer);
                        staffPlayer.sendMessage(logs);

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
