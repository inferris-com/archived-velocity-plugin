package com.inferris.commands;

import com.inferris.database.DatabasePool;
import com.inferris.player.*;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.context.PlayerContextFactory;
import com.inferris.player.PlayerData;
import com.inferris.player.service.PlayerDataManager;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.util.ChatUtil;
import com.inferris.util.DatabaseUtils;
import com.inferris.util.UnixTimeUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public class CommandFlagPlayer extends Command {
    private final PlayerDataService playerDataService;

    public CommandFlagPlayer(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            PlayerContext playerContext = PlayerContextFactory.create(player.getUniqueId(), playerDataService);
            return playerContext.getRank().getBranchValue(Branch.STAFF) >= 2;
        }
        // Allow console to execute the command
        return sender.getName().equalsIgnoreCase("CONSOLE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        PlayerDataService playerDataService = ServiceLocator.getPlayerDataService();

        if (args.length < 1) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /flag <add|remove> <player> [reason]"));
            return;
        }

        UUID flaggedByUuid = null;
        if (sender instanceof ProxiedPlayer player) {
            flaggedByUuid = player.getUniqueId();
        }

        String action = args[0].toLowerCase();

        if ("list".equals(action)) {
            listFlaggedPlayers(sender);
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /flag <add|remove|list> <player> [reason]"));
            return;
        }

        String playerName = args[1];
        String reason = args.length > 2 ? buildReason(args) : null;

        ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(playerName);
        UUID targetUuid = null;
        PlayerData targetPlayerData = null;

        if (targetPlayer != null) {
            targetUuid = targetPlayer.getUniqueId();
            targetPlayerData = PlayerDataManager.getInstance().getPlayerData(targetUuid);
        } else {
            targetUuid = playerDataService.fetchUUIDByUsername(playerName);
            if (targetUuid != null) {
                targetPlayerData = PlayerDataManager.getInstance().getPlayerData(targetUuid);
            }
        }

        if (targetUuid == null) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Player not found."));
            return;
        }

        playerName = targetPlayerData.getUsername();

        try (Connection connection = DatabasePool.getConnection()) {
            if ("add".equals(action)) {
                addFlaggedPlayer(connection, targetUuid, reason, flaggedByUuid, targetPlayerData, sender);
            } else if ("remove".equals(action)) {
                removeFlaggedPlayer(connection, targetUuid, targetPlayerData, sender);
            } else if ("view".equalsIgnoreCase(action)) {
                viewFlaggedPlayer(connection, targetUuid, targetPlayerData, sender);
            } else {
                sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /flag <add|remove> <player> [reason]"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while processing the command."));
        }
    }

    private String buildReason(String[] args) {
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }
        return reasonBuilder.toString().trim();
    }

    private void addFlaggedPlayer(Connection connection, UUID targetUuid, String reason, UUID flaggedByUuid, PlayerData targetPlayerData, CommandSender sender) throws SQLException {
        String[] columns = {"uuid", "reason", "flagged_by_uuid", "date"};
        String condition = "`uuid` = ?";
        Object[] parameters = {targetUuid.toString()};

        ResultSet resultSet = DatabaseUtils.executeQuery(connection, "flagged_players", columns, condition, parameters);
        if (!resultSet.next()) {
            Object[] insert = {
                    targetUuid.toString(),
                    reason,
                    flaggedByUuid != null ? flaggedByUuid.toString() : null,
                    Instant.now().getEpochSecond()
            };

            DatabaseUtils.insertData(connection, "flagged_players", columns, insert);
            DatabaseUtils.updateData(connection, "profile", new String[]{"is_flagged"}, new Object[]{1}, "`uuid` = '" + targetUuid + "'");
            if (targetPlayerData != null) {
                targetPlayerData.getProfile().setFlagged(true);
                PlayerDataManager.getInstance().updateAllDataAndPush(targetUuid, targetPlayerData);
            }
            sender.sendMessage(new TextComponent(ChatColor.GREEN + "Player flagged successfully."));
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Player is already on the list!"));
        }
    }

    private void removeFlaggedPlayer(Connection connection, UUID targetUuid, PlayerData targetPlayerData, CommandSender sender) throws SQLException {
        String condition = "`uuid` = ?";
        Object[] parameters = {targetUuid.toString()};

        int rowsAffected = DatabaseUtils.executeDelete(connection, "flagged_players", condition, parameters);
        DatabaseUtils.updateData(connection, "profile", new String[]{"is_flagged"}, new Object[]{0}, "`uuid` = '" + targetUuid + "'");
        if (rowsAffected > 0) {
            if (targetPlayerData != null) {
                targetPlayerData.getProfile().setFlagged(false);
                PlayerDataManager.getInstance().updateAllDataAndPush(targetUuid, targetPlayerData);
            }
            sender.sendMessage(new TextComponent(ChatColor.GREEN + "Player unflagged successfully."));
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Player was not on the list."));
        }
    }

    private void listFlaggedPlayers(CommandSender sender) {
        try (Connection connection = DatabasePool.getConnection()) {
            String query = "SELECT uuid, reason, flagged_by_uuid, date FROM flagged_players ORDER BY date DESC";
            ResultSet resultSet = connection.prepareStatement(query).executeQuery();

            sender.sendMessage(new TextComponent(ChatColor.GOLD + "Flagged Players:"));

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String reason = resultSet.getString("reason");
                UUID flaggedByUuid = resultSet.getString("flagged_by_uuid") != null ? UUID.fromString(resultSet.getString("flagged_by_uuid")) : null;

                PlayerData targetPlayerData = PlayerDataManager.getInstance().getPlayerData(uuid);
                PlayerData moderatorPlayerData = PlayerDataManager.getInstance().getPlayerData(flaggedByUuid);
                String moderatorRank = PlayerDataManager.getInstance().getPlayerData(flaggedByUuid).getRank().getByBranch().getPrefix(true);

                TextComponent clickableUsername = ChatUtil.createClickableTextComponent(
                        ChatColor.RED + targetPlayerData.getUsername(),
                        ChatColor.GREEN + "Click to view account",
                        "/account " + targetPlayerData.getUsername(),
                        ClickEvent.Action.RUN_COMMAND
                );

                TextComponent mainMessage = new TextComponent();
                mainMessage.addExtra(clickableUsername);
                mainMessage.addExtra(new TextComponent(TextComponent.fromLegacyText(ChatColor.RESET + " flagged by " + moderatorRank + ChatColor.RESET + moderatorPlayerData.getUsername() +
                        (reason != null ? ChatColor.RESET + " for '" + ChatColor.GREEN + reason + ChatColor.RESET + "'" : ""))));

                sender.sendMessage(mainMessage);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while retrieving flagged players."));
        }
    }

    private void viewFlaggedPlayer(Connection connection, UUID targetUuid, PlayerData targetPlayerData, CommandSender sender) throws SQLException {
        String[] columnNames = new String[]{"reason", "flagged_by_uuid", "date"};
        String condition = "`uuid` = ?";
        Object[] parameters = {targetUuid.toString()};

        ResultSet resultSet = DatabaseUtils.executeQuery(connection, "flagged_players", columnNames, condition, parameters);

        if (resultSet.next()) {
            String reason = resultSet.getString("reason");
            UUID flaggedByUuid = resultSet.getString("flagged_by_uuid") != null ? UUID.fromString(resultSet.getString("flagged_by_uuid")) : null;
            long timestamp = resultSet.getLong("date");

            PlayerData moderatorPlayerData = PlayerDataManager.getInstance().getPlayerData(flaggedByUuid);
            String moderatorRank = PlayerDataManager.getInstance().getPlayerData(flaggedByUuid).getRank().getByBranch().getPrefix(true);

            TextComponent textComponent = new TextComponent();
            textComponent.addExtra(new TextComponent(ChatColor.GRAY + "Username: " + ChatColor.RESET + targetPlayerData.getUsername() + "\n"));
            textComponent.addExtra(new TextComponent(ChatColor.GRAY + "Reason: " + ChatColor.RED + (reason != null ? reason : "None") + "\n"));
            textComponent.addExtra(new TextComponent(TextComponent.fromLegacyText(ChatColor.GRAY + "Flagged By: " + ChatColor.RESET + (flaggedByUuid != null ? moderatorRank
                    + ChatColor.RESET + moderatorPlayerData.getUsername() : "Unknown") + "\n")));
            textComponent.addExtra(new TextComponent(ChatColor.GRAY + "Date: " + ChatColor.YELLOW + UnixTimeUtils.getDateOnly(timestamp)
                    + ChatColor.RESET + " at " + ChatColor.YELLOW + UnixTimeUtils.getTimeOnly(timestamp) + "\n"));
            textComponent.addExtra(new TextComponent(ChatColor.GRAY + "----------------------------"));

            sender.sendMessage(textComponent);
        } else {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Error: That player is not flagged!"));
        }
    }
}