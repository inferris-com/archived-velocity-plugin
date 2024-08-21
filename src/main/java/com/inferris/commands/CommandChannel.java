package com.inferris.commands;

import com.google.inject.Inject;
import com.inferris.player.channel.Channel;
import com.inferris.player.channel.ChannelManager;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.manager.ManagerContainer;
import com.inferris.player.service.PlayerDataService;
import com.inferris.server.Message;
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
    private final PlayerDataService playerDataService;
    private final ManagerContainer managerContainer;

    @Inject
    public CommandChannel(PlayerDataService playerDataService, ManagerContainer managerContainer) {
        super("channel", null, "ch");
        this.playerDataService = playerDataService;
        this.managerContainer = managerContainer;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;
            ChannelManager channelManager = managerContainer.getChannelManager();

            PlayerContext playerContext = new PlayerContext(player.getUniqueId(), playerDataService);

            if (length != 1) {
                player.sendMessage(new TextComponent(ChatColor.RED + "/channel <channel>"));
                return;
            }
            String channel = args[0].toLowerCase();

            switch (channel) {
                case "staff" -> {
                    if (playerContext.getRank().getBranchValue(Branch.STAFF) >= 1) {
                        channelManager.setChannel(player, Channel.STAFF, true);
                    } else {
                        player.sendMessage(Message.NO_PERMISSION.getMessage());
                    }
                }
                case "admin" -> {
                    if(playerContext.getRank().getBranchValue(Branch.STAFF) >=3){
                        channelManager.setChannel(player, Channel.ADMIN, true);
                    }else{
                        player.sendMessage(Message.NO_PERMISSION.getMessage());
                    }
                }
                case "special" -> channelManager.setChannel(player, Channel.SPECIAL, true);
                case "none" -> channelManager.setChannel(player, Channel.NONE, true);
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
