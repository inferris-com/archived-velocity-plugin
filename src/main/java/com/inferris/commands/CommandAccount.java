package com.inferris.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.inferris.Inferris;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.UUID;

public class CommandAccount extends Command {
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
                String username = args[0];
                PlayerDataManager playerDataManager = PlayerDataManager.getInstance();

                UUID uuid = playerDataManager.getUUIDByUsername(username);
                if (uuid != null) {
                    PlayerData playerData = playerDataManager.getRedisData(uuid, username);

                    player.sendMessage(new TextComponent("Match found for username: " + playerData.getRegistry().getUsername()));
                    player.sendMessage(new TextComponent("UUID: " + uuid));
                    player.sendMessage(new TextComponent(playerData.getCoins().toString()));
                    player.sendMessage(new TextComponent(playerData.getByBranch().getPrefix()));
                }
            }
        }
    }
}

//    @Override
//    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
//        return null;
//    }