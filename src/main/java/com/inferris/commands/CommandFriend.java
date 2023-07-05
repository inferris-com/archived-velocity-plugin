package com.inferris.commands;

import com.inferris.Messages;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class CommandFriend extends Command {
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
            if (length == 3) {
                UUID uuid = PlayerDataManager.getInstance().getUUIDByUsername(args[1]);
                if(uuid == null){
                    player.sendMessage(new TextComponent(Messages.PLAYER_NOT_IN_SYSTEM.getMessage()));
                    return;
                }
                PlayerData playerData = PlayerDataManager.getInstance().getRedisData(uuid, args[0]);
                String targetName = playerData.getRegistry().getUsername();
                if (args[0].equalsIgnoreCase("add")) {
                    
                }
            }
        }
    }
}
