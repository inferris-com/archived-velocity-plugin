package com.inferris.commands;

import com.inferris.Messages;
import com.inferris.player.ChannelManager;
import com.inferris.player.Channels;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Branch;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;

public class CommandChannel extends Command implements TabExecutor {
    public CommandChannel(String name) {
        super(name, null, "ch");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer player){
            int length = args.length;

            if(length != 1){
                player.sendMessage(new TextComponent(ChatColor.RED + "/channel <channel>"));
                return;
            }
            ChannelManager channelManager = new ChannelManager();
            String channel = args[0].toLowerCase();

            switch (channel){
                case "staff" -> {
                    if(PlayerDataManager.getInstance().getPlayerData(player).getBranchValue(Branch.STAFF) >=1){
                        channelManager.setChannel(player, Channels.STAFF, true);
                    }else{
                        player.sendMessage(Messages.NO_PERMISSION.getMessage());
                    }
                }
                case "special" -> channelManager.setChannel(player, Channels.SPECIAL, true);
                case "none" -> channelManager.setChannel(player, Channels.NONE, true);
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        if(commandSender instanceof ProxiedPlayer player){
            ArrayList<String> options = new ArrayList<>();
            options.add("staff");
            options.add("special");
            options.add("none");
            return options;
        }
        return Collections.emptyList();
    }
}
