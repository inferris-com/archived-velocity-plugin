package com.inferris.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.server.JedisChannels;
import com.inferris.util.CacheSerializationUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

public class ChannelManager {

    public void setChannel(ProxiedPlayer player, Channels channel, boolean sendMessage){
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
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

        PlayerDataManager.getInstance().updateAllData(player, playerData);
        try(Jedis jedis = Inferris.getJedisPool().getResource()){
            jedis.publish(JedisChannels.PLAYERDATA_CACHE_UPDATE.name(), CacheSerializationUtils.serializePlayerData(playerData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
