package com.inferris.messaging;

import java.util.UUID;

public class StaffChatMessage {
    private UUID playerUUID;
    private String message;

    public StaffChatMessage(UUID playerUUID, String message){
        this.playerUUID = playerUUID;
        this.message = message;
    }

    public StaffChatMessage(){}

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
