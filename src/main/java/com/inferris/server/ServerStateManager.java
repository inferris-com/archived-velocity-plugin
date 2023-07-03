package com.inferris.server;

public class ServerStateManager {
    private static ServerState currentState = ServerState.NORMAL;

    public static ServerState getCurrentState() {
        return currentState;
    }

    public static void setCurrentState(ServerState currentState) {
        ServerStateManager.currentState = currentState;
    }
}