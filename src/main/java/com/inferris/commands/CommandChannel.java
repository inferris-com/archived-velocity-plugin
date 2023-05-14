package com.inferris.commands;

import com.inferris.channel.ChannelManager;
import com.inferris.channel.Channels;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandChannel extends Command {

    public CommandChannel(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer player){
            int length = args.length;
            ChannelManager channelManager = ChannelManager.getInstance();

            if(length != 1){
                player.sendMessage("§c/channel <channel>");
                return;
            }else{
                String channel = args[0];
                switch (channel){
                    case "staff" -> channelManager.setChannel(player, Channels.STAFF);
                    case "special" -> channelManager.setChannel(player, Channels.SPECIAL);
                    case "none" -> channelManager.setChannel(player, Channels.NONE);
                }
            }
        }
    }
}
