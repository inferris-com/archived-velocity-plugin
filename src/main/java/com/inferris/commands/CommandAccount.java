package com.inferris.commands;

import com.inferris.Inferris;
import com.inferris.database.Database;
import com.inferris.database.DatabasePool;
import com.inferris.player.*;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.util.DatabaseUtils;
import com.inferris.util.MessageUtil;
import com.inferris.server.Tag;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CommandAccount extends Command implements TabExecutor {
    protected final PlayerDataService playerDataService;
    public CommandAccount(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return ServiceLocator.getPlayerDataService().getPlayerData(player.getUniqueId()).getRank().getBranchValue(Branch.STAFF) >= 1;
        }
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if (length == 0) {
                player.sendMessage(new TextComponent(ChatColor.YELLOW + "(Staff command) " + ChatColor.RED + "Usage: /account <user>"));
                return;
            }
            if (length == 1) {
                String targetName = args[0];
                PlayerDataManager playerDataManager = PlayerDataManager.getInstance();
                PlayerDataService playerDataService = ServiceLocator.getPlayerDataService();

                UUID uuid = playerDataService.fetchUUIDByUsername(args[0]);
                PlayerContext playerContext = PlayerContextFactory.create(uuid, playerDataService);
                Rank rank = playerContext.getRank();
                String tag = Tag.STAFF.getName(true);
                ChatColor reset = ChatColor.RESET;

                TextComponent header = new TextComponent("----- Player Information -----");
                header.setColor(ChatColor.GOLD);
                header.setBold(true);

                TextComponent username = new TextComponent(ChatColor.YELLOW + "Username: ");
                username.addExtra(playerContext.getNameColor() + playerContext.getUsername());
                username.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to copy username")));
                username.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, playerContext.getUsername()));

                TextComponent prefix = new TextComponent(TextComponent.fromLegacyText("Ranks: " + rank.getFormattedApplicableRanks()));
                prefix.setColor(ChatColor.YELLOW);

                TextComponent registration_date = new TextComponent(ChatColor.YELLOW + "Registration date: " + reset
                        + playerContext.getProfile().getFormattedRegistrationDate("MMMM dd, yyyy") + " at " + playerContext.getProfile().getRegistrationTimeOnly());

                String xenforoUsername = ChatColor.YELLOW + "XenForo username: " + ChatColor.RESET;
                try (Connection connection = DatabasePool.getConnection(Database.XENFORO);
                     ResultSet resultSet = DatabaseUtils.executeQuery(connection, "xf_user", new String[]{"username"}, "`user_id` = ?", playerContext.getProfile().getXenforoId())) {

                    if (resultSet.next()) {
                        xenforoUsername = xenforoUsername + resultSet.getString(1);
                    }
                } catch (SQLException e) {
                    Inferris.getInstance().getLogger().severe(e.getMessage());
                }

                TextComponent verified = new TextComponent(ChatColor.YELLOW + "XenForo ID: " + ChatColor.RESET + playerContext.getProfile().getXenforoId());

                TextComponent channel = new TextComponent(ChatColor.YELLOW + "Current channel: " + reset + playerContext.getChannel().getTag());

                TextComponent vanished;
                if (playerContext.getVanishState() == VanishState.ENABLED) {
                    vanished = new TextComponent(ChatColor.YELLOW + "Vanish state: " + reset + ChatColor.GREEN + playerContext.getVanishState());
                } else {
                    vanished = new TextComponent(ChatColor.YELLOW + "Vanish state: " + reset + ChatColor.RED + playerContext.getVanishState());
                }

                TextComponent divider = new TextComponent("-------------------------------");
                divider.setColor(ChatColor.GOLD);

                TextComponent uuidText = new TextComponent(ChatColor.YELLOW + "UUID: " + reset + playerContext.getUuid());
                uuidText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to copy UUID")));
                uuidText.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, playerContext.getUuid().toString()));

                TextComponent coins = new TextComponent(ChatColor.YELLOW + "Coins: " + reset + playerContext.getCoins());
                TextComponent staff;
                TextComponent misc = new TextComponent(ChatColor.GOLD + "\nProfile info");
                TextComponent bio = new TextComponent(ChatColor.YELLOW + "Bio: " + reset + playerContext.getProfile().getBio());
                TextComponent pronouns = new TextComponent(ChatColor.YELLOW + "Pronouns: " + reset + playerContext.getProfile().getPronouns());

                if(playerContext.isStaff()) {
                    staff = new TextComponent(ChatColor.YELLOW + "Staff: " + ChatColor.AQUA + playerContext.isStaff());
                }else{
                    staff = new TextComponent(ChatColor.YELLOW + "Staff: " + reset + playerContext.isStaff());
                }

                player.sendMessage(header);
                player.sendMessage(new TextComponent(""));
                MessageUtil.sendMessage(player, username);
                MessageUtil.sendMessage(player, prefix);
                MessageUtil.sendMessage(player, uuidText);
                MessageUtil.sendMessage(player, registration_date);
                MessageUtil.sendMessage(player, coins);
                if(playerContext.getProfile().getXenforoId() > 0){
                    MessageUtil.sendMessage(player, xenforoUsername);
                    MessageUtil.sendMessage(player, verified);
                }
                MessageUtil.sendMessage(player, channel);
                MessageUtil.sendMessage(player, vanished);
                MessageUtil.sendMessage(player, staff);
                player.sendMessage(divider);
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            if (args.length == 1) {
                String partialPlayerName = args[0];
                List<String> completions = new ArrayList<>();

                for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                    String playerName = proxiedPlayers.getName();
                    if (playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())) {
                        completions.add(playerName);
                    }
                }
                return completions;
            }
        }
        return Collections.emptyList();
    }
}

//    @Override
//    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
//        return null;
//    }