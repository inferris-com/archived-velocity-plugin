package com.inferris.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.server.JedisChannels;
import com.inferris.util.CacheSerializationUtils;
import com.inferris.util.DatabaseUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import redis.clients.jedis.Jedis;

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
            if (length == 0) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /vanish <on:off>"));
            }
            if (length == 1) {
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
            }
        }
    }

    private void updateDatabase(ProxiedPlayer player, int isVanished) {
        String sql = "UPDATE players SET vanished = ? WHERE uuid = ?";
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
        PlayerDataManager.getInstance().updateAllData(player, playerData);
        String json;
        try {
            json = CacheSerializationUtils.serializePlayerData(playerData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        /* Publishes the player data update so that Inferris front-end can pick it up
        and update their caches accordingly */
        Inferris.getInstance().getLogger().info(".....");
        try(Jedis jedis = Inferris.getJedisPool().getResource()){
            jedis.publish(JedisChannels.PLAYERDATA_VANISH.getChannelName(), json);
        }
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            String partialOption = args[0].toLowerCase();
            List<String> options = new ArrayList<>();

            List<String> availableOptions = Arrays.asList("off", "on");

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
