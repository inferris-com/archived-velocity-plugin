package com.inferris.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.server.Messages;
import com.inferris.server.jedis.JedisChannels;
import com.inferris.server.jedis.JedisHelper;
import com.inferris.util.SerializationUtils;
import com.inferris.util.DatabaseUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandVanish extends Command implements TabExecutor {
    public CommandVanish(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        if (sender instanceof ProxiedPlayer player) {
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);

            if (playerData.getBranchValue(Branch.STAFF) >= 3) {
                if (length == 0 || length > 1) {
                    player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /vanish <on:off>"));
                    return;
                }

                if (args[0].equalsIgnoreCase("on")) {
                    updateDatabase(player, 1);
                    updatePlayerData(player, VanishState.ENABLED);
                    //BungeeUtils.sendBungeeMessage(player, BungeeChannel.PLAYER_DATA, Subchannel.VANISH, Subchannel.FORWARD, VanishState.ENABLED.name());
                }
                if (args[0].equalsIgnoreCase("off")) {
                    updateDatabase(player, 0);
                    updatePlayerData(player, VanishState.DISABLED);
                    //BungeeUtils.sendBungeeMessage(player, BungeeChannel.PLAYER_DATA, Subchannel.VANISH, Subchannel.FORWARD, VanishState.ENABLED.name());
                }

                if(args[0].equalsIgnoreCase("join")){
                    try(Jedis jedis = Inferris.getJedisPool().getResource()){
                        jedis.publish(JedisChannels.PLAYERDATA_VANISH.getChannelName(), player.getUniqueId().toString() + ":join");
                    }
                }

                if(args[0].equalsIgnoreCase("quit")){
                    try(Jedis jedis = Inferris.getJedisPool().getResource()){
                        jedis.publish(JedisChannels.PLAYERDATA_VANISH.getChannelName(), player.getUniqueId().toString() + ":quit");
                    }
                }
            } else {
                player.sendMessage(Messages.NO_PERMISSION.getMessage());
            }
        }
    }

    private void updateDatabase(ProxiedPlayer player, int isVanished) {
        String sql = "UPDATE player_data SET vanished = ? WHERE uuid = ?";
        Object vanished = isVanished;
        Object uuid = player.getUniqueId().toString();

        try {
            int affectedRows = DatabaseUtils.executeUpdate(sql, vanished, uuid);
            Inferris.getInstance().getLogger().info("Affected rows: " + affectedRows);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePlayerData(ProxiedPlayer player, VanishState vanishState) {
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
        playerData.setVanishState(vanishState);
        PlayerDataManager.getInstance().updateAllDataAndPush(player, playerData, JedisChannels.PLAYERDATA_VANISH);
        String json;
        try {
            json = SerializationUtils.serializePlayerData(playerData);
            Inferris.getInstance().getLogger().warning("Bungee json: " + json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        /* Publishes the player data update so that Inferris front-end can pick it up
        and update their caches accordingly */

//        Inferris.getInstance().getLogger().info(".....");
//        try(Jedis jedis = Inferris.getJedisPool().getResource()){
//            jedis.publish(JedisChannels.PLAYERDATA_VANISH.getChannelName(), json);
//        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            String partialOption = args[0].toLowerCase();
            List<String> options = new ArrayList<>();

            List<String> availableOptions = Arrays.asList("off", "on", "join", "quit");

            for (String option : availableOptions) {
                if (option.toLowerCase().startsWith(partialOption)) {
                    options.add(option);
                }
            }
            return options;
        }
        return Collections.emptyList();
    }
}
