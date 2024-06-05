package com.inferris.commands;

import com.inferris.player.Channel;
import com.inferris.player.ChannelManager;
import com.inferris.player.PlayerDataManager;
import com.inferris.util.ChatUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandStaffchatShortcut extends Command {
    public CommandStaffchatShortcut(String name) {
        super(name, null, "s");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        StringBuilder message = new StringBuilder();
        BaseComponent[] textComponent = null;

        for (String word : args) {
            message.append(word).append(" "); // Add a space between words
        }

        if (sender instanceof ProxiedPlayer player) {
            ChannelManager.sendStaffChatMessage(Channel.STAFF, message.toString(), ChannelManager.StaffChatMessageType.PLAYER, player.getUniqueId());
        }else{
            ChannelManager.sendStaffChatMessage(Channel.STAFF, message.toString(), ChannelManager.StaffChatMessageType.CONSOLE);
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return PlayerDataManager.getInstance().getPlayerData(player).isStaff();
        }
        return true;
    }
}