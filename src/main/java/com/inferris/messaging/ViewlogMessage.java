package com.inferris.messaging;

import com.inferris.server.Server;

import java.util.List;
import java.util.UUID;

public class ViewlogMessage {
    private UUID uniqueRequestId;
    private UUID playerUUID;
    private String requestedServer;
    private Server currentServer;
    private long timestamp;
    private List<String> chatLogMessages;

    public ViewlogMessage(UUID uniqueRequestId, UUID playerUUID, String requestedServer, Server currentServer, long timestamp, List<String> chatLogMessages){
        this.uniqueRequestId = uniqueRequestId;
        this.playerUUID = playerUUID;
        this.requestedServer = requestedServer;
        this.currentServer = currentServer;
        this.timestamp = timestamp;
        this.chatLogMessages = chatLogMessages;
    }

    public ViewlogMessage(){}

    public UUID getUniqueRequestId() {
        return uniqueRequestId;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getRequestedServer() {
        return requestedServer;
    }

    public Server getCurrentServer() {
        return currentServer;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<String> getChatLogMessages() {
        return chatLogMessages;
    }

    public void setUniqueRequestId(UUID uniqueRequestId) {
        this.uniqueRequestId = uniqueRequestId;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void setRequestedServer(String requestedServer) {
        this.requestedServer = requestedServer;
    }

    public void setCurrentServer(Server currentServer) {
        this.currentServer = currentServer;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setChatLogMessages(List<String> chatLogMessages) {
        this.chatLogMessages = chatLogMessages;
    }
}
