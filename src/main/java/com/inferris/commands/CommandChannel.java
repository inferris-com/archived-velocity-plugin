package com.inferris.commands;

import com.inferris.server.Message;
import com.inferris.player.ChannelManager;
import com.inferris.player.Channel;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Branch;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandChannel extends Command implements TabExecutor {
    public CommandChannel(String name) {
        super(name, null, "ch");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if (length != 1) {
                player.sendMessage(new TextComponent(ChatColor.RED + "/channel <channel>"));
                return;
            }
            String channel = args[0].toLowerCase();

            switch (channel) {
                case "staff" -> {
                    if (PlayerDataManager.getInstance().getPlayerData(player).getBranchValue(Branch.STAFF) >= 1) {
                        ChannelManager.setChannel(player, Channel.STAFF, true);
                    } else {
                        player.sendMessage(Message.NO_PERMISSION.getMessage());
                    }
                }
                case "admin" -> {
                    if(PlayerDataManager.getInstance().getPlayerData(player).getBranchValue(Branch.STAFF) >=3){
                        ChannelManager.setChannel(player, Channel.ADMIN, true);
                    }else{
                        player.sendMessage(Message.NO_PERMISSION.getMessage());
                    }
                }
                case "special" -> ChannelManager.setChannel(player, Channel.SPECIAL, true);
                case "none" -> ChannelManager.setChannel(player, Channel.NONE, true);
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            String partialOption = args[0].toLowerCase();
            List<String> options = new ArrayList<>();

            List<String> availableOptions = Arrays.asList("staff", "admin", "special", "none");

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
