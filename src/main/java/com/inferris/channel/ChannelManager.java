package com.inferris.channel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class ChannelManager {
    private static ChannelManager instance;
    private final Cache<UUID,Channels> channelCache;

    private ChannelManager() {
        channelCache = CacheBuilder.newBuilder().build();
    }

    public synchronized static ChannelManager getInstance() {
        if(instance == null) {
            instance = new ChannelManager();
        }
        return instance;
    }

    public void setChannel(ProxiedPlayer player, Channels channel){
        getChannelCache().invalidate(player.getUniqueId());
        getChannelCache().put(player.getUniqueId(), channel);

        String channelName = null;

        switch (channel) {
            case STAFF -> channelName = ChatColor.AQUA + String.valueOf(channel);
            case SPECIAL -> channelName = ChatColor.GOLD + String.valueOf(channel);
        }
        if(channel == Channels.NONE){
            getChannelCache().invalidate(player.getUniqueId());
        }

        player.sendMessage(new TextComponent(ChatColor.YELLOW + "Channel set to " + channelName));
    }

    public void sendMessage(ProxiedPlayer player, Channels channels, String message){
        switch (channels){
            case STAFF -> {
                for(ProxiedPlayer all : ProxyServer.getInstance().getPlayers()){
                    if(getChannel(all) == Channels.STAFF){
                        all.sendMessage(new TextComponent(channels.getTag(true) + player.getName() + ChatColor.YELLOW  + ": " + message));
                    }
                }
            }
            case SPECIAL -> {
                for(ProxiedPlayer all : ProxyServer.getInstance().getPlayers()){
                    if(getChannel(all) == Channels.SPECIAL){
                        all.sendMessage(new TextComponent(channels.getTag(true) + player.getName() + ChatColor.YELLOW  + ": " + message));
                    }
                }
            }
        }
    }

    public Channels getChannel(ProxiedPlayer player){
        try{
            return getChannelCache().get(player.getUniqueId(), () -> {
                getChannelCache().put(player.getUniqueId(), Channels.NONE);
                return getChannel(player);
            });
        }catch(Exception e){
            e.printStackTrace();
        }
        return Channels.NONE;
    }

    public void invalidate(ProxiedPlayer player){
        getChannelCache().invalidate(player.getUniqueId());
    }

    public Cache<UUID, Channels> getChannelCache() {
        return channelCache;
    }
}
