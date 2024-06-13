package com.inferris.messaging;

import com.inferris.player.channel.Channel;

import java.util.UUID;

public class StaffChatMessage {
    private UUID playerUUID;
    private Channel channel;
    private String message;

    public StaffChatMessage(UUID playerUUID, Channel channel, String message){
        this.playerUUID = playerUUID;
        this.channel = channel;
        this.message = message;
    }

    public StaffChatMessage(){}

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
