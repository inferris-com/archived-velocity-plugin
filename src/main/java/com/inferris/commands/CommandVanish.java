package com.inferris.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.registry.RegistryManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.server.BungeeChannel;
import com.inferris.server.Subchannel;
import com.inferris.util.BungeeUtils;
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
                    //BungeeUtils.sendBungeeMessage(player, BungeeChannel.PLAYER_DATA, Subchannel.VANISH, Subchannel.FORWARD, VanishState.ENABLED.name());
                    updateDatabase(player, 1);
                    updatePlayerData(player, VanishState.ENABLED);
                }
                if (args[0].equalsIgnoreCase("off")) {
                    //BungeeUtils.sendBungeeMessage(player, BungeeChannel.PLAYER_REGISTRY, Subchannel.VANISH, Subchannel.FORWARD, VanishState.DISABLED.name());
                    updateDatabase(player, 0);
                    updatePlayerData(player, VanishState.DISABLED);
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
        playerData.getRegistry().setVanishState(vanishState);
        PlayerDataManager.getInstance().updateRedisData(player, playerData);
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
            jedis.publish("playerdata_update", json);
        }
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player && args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("on");
            list.add("off");
            return list;
        }
        return Collections.emptyList();
    }
}
