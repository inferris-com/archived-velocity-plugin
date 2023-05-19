package com.inferris.commands;

import com.inferris.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandVanish extends Command {
    public CommandVanish(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        if(sender instanceof ProxiedPlayer player){
            if(length == 0){
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /vanish <on:off>"));
            }
            if(length == 1){
                if(args[0].equalsIgnoreCase("on")){

                }
            }
        }
    }
}
