package com.inferris.commands;

import com.google.inject.Inject;
import com.inferris.player.channel.Channel;
import com.inferris.player.channel.ChannelManager;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.service.ManagerContainer;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandAdminchatShortcut extends Command {
    private final PlayerDataService playerDataService;
    private final ManagerContainer managerContainer;

    @Inject
    public CommandAdminchatShortcut(PlayerDataService playerDataService, ManagerContainer managerContainer) {
        super("ac", null, "a");
        this.playerDataService = playerDataService;
        this.managerContainer = managerContainer;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        StringBuilder message = new StringBuilder();
        BaseComponent[] textComponent = null;
        ChannelManager channelManager = managerContainer.getChannelManager();

        if(args.length == 0){
            sender.sendMessage(new TextComponent(ChatColor.RED + "You must provide a message!"));
            return;
        }

        for (String word : args) {
            message.append(word).append(" "); // Add a space between words
        }

        if (sender instanceof ProxiedPlayer player) {
            channelManager.sendStaffChatMessage(Channel.ADMIN, message.toString(), ChannelManager.StaffChatMessageType.PLAYER, player.getUniqueId());
        }else{
            channelManager.sendStaffChatMessage(Channel.ADMIN, message.toString(), ChannelManager.StaffChatMessageType.CONSOLE);
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            PlayerContext playerContext = new PlayerContext(player.getUniqueId(), playerDataService);
            return playerContext.getRank().getBranchValue(Branch.STAFF) >= 3;
        }
        return true;
    }
}