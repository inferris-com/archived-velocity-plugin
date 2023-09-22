package com.inferris.commands;

import com.inferris.database.Database;
import com.inferris.database.DatabasePool;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.registry.Registry;
import com.inferris.player.vanish.VanishState;
import com.inferris.util.DatabaseUtils;
import com.inferris.util.MessageUtil;
import com.inferris.util.Tags;
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
    public CommandAccount(String name) {
        super(name);
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

                UUID uuid = playerDataManager.getUUIDByUsername(targetName);
                if (uuid == null) {
                    player.sendMessage(new TextComponent(ChatColor.RED + "That player is not in our system."));
                    return;
                }

                PlayerData playerData = playerDataManager.getRedisData(uuid, targetName);
                Registry registry = playerData.getRegistry();
                String tag = Tags.STAFF.getName(true);
                ChatColor reset = ChatColor.RESET;

                TextComponent header = new TextComponent("----- Player Information -----");
                header.setColor(ChatColor.GOLD);
                header.setBold(true);

                TextComponent username = new TextComponent(ChatColor.YELLOW + "Username: ");
                username.addExtra(playerData.getNameColor() + registry.getUsername());
                username.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to copy username")));
                username.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, registry.getUsername()));


                TextComponent prefix = new TextComponent("Ranks: " + playerData.formatRankList(playerData.getTopRanksByBranches()));
                prefix.setColor(ChatColor.YELLOW);

                TextComponent registration_date = new TextComponent(ChatColor.YELLOW + "Registration date: " + reset + playerData.getProfile().getRegistrationDate());

                TextComponent verified = new TextComponent(ChatColor.YELLOW + "Forum account: " + ChatColor.RESET + "ID " + playerData.getProfile().getXenforoId());
                String[] verifiedParams = {};
                String xenforoUsername = null;
                try (Connection connection = DatabasePool.getConnection(Database.XENFORO);
                     ResultSet resultSet = DatabaseUtils.executeQuery(connection, "xf_user", new String[]{"username"}, "`user_id` = ?", playerData.getProfile().getXenforoId())) {

                    if (resultSet.next()) {
                        xenforoUsername = resultSet.getString(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                TextComponent channel = new TextComponent(ChatColor.YELLOW + "Current channel: " + reset + playerData.getChannel().getMessage());

                TextComponent vanished;
                if (playerData.getVanishState() == VanishState.ENABLED) {
                    vanished = new TextComponent(ChatColor.YELLOW + "Vanish state: " + reset + ChatColor.GREEN + playerData.getVanishState());
                } else {
                    vanished = new TextComponent(ChatColor.YELLOW + "Vanish state: " + reset + ChatColor.RED + playerData.getVanishState());
                }

                TextComponent divider = new TextComponent("-------------------------------");
                divider.setColor(ChatColor.GOLD);

                TextComponent uuidText = new TextComponent(ChatColor.YELLOW + "UUID: " + reset + uuid);
                uuidText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to copy UUID")));
                uuidText.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString()));

                TextComponent coins = new TextComponent(ChatColor.YELLOW + "Coins: " + reset + playerData.getCoins().getBalance());
                TextComponent staff;
                TextComponent misc = new TextComponent(ChatColor.GOLD + "\nProfile info");
                TextComponent bio = new TextComponent(ChatColor.YELLOW + "Bio: " + reset + playerData.getProfile().getBio());
                TextComponent pronouns = new TextComponent(ChatColor.YELLOW + "Pronouns: " + reset + playerData.getProfile().getPronouns());

                if(playerData.isStaff()) {
                    staff = new TextComponent(ChatColor.YELLOW + "Staff: " + ChatColor.AQUA + playerData.isStaff());
                }else{
                    staff = new TextComponent(ChatColor.YELLOW + "Staff: " + reset + playerData.isStaff());
                }

                player.sendMessage(header);
                player.sendMessage(new TextComponent(""));
                MessageUtil.sendMessage(player, username);
                MessageUtil.sendMessage(player, prefix);
                MessageUtil.sendMessage(player, uuidText);
                MessageUtil.sendMessage(player, registration_date);
                MessageUtil.sendMessage(player, coins);
                if(playerData.getProfile().getXenforoId() > 0){
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