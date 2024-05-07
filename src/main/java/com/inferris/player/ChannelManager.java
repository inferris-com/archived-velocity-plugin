package com.inferris.player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ChannelManager {

    public void setChannel(ProxiedPlayer player, Channels channel, boolean sendMessage){
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player, "#setChannel, ChannelManager");
        playerData.setChannel(channel);
        String channelName = null;

        if(sendMessage){
            switch (channel){
                case STAFF -> channelName = ChatColor.AQUA + String.valueOf(channel);
                case SPECIAL -> channelName = ChatColor.GOLD + String.valueOf(channel);
                case NONE -> channelName = ChatColor.GRAY + String.valueOf(channel);
            }
            player.sendMessage(new TextComponent(ChatColor.YELLOW + "Channel set to " + channelName));
        }

        PlayerDataManager.getInstance().updateAllDataAndPush(player, playerData);
    }
}
