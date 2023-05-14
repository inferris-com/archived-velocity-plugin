package com.inferris.channel;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Channel {
    private final ProxiedPlayer player;
    private Channel channel;
    public Channel(ProxiedPlayer player, Channel channel){
        this.player = player;
        this.channel = channel;
    }

    public void setChannel(Channel newChannel){
        this.channel = newChannel;
    }
}
