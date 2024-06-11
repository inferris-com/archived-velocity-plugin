package com.inferris.player;

public class ServiceLocator {
    private static PlayerDataService playerDataService;

    public static void setPlayerDataService(PlayerDataService service) {
        playerDataService = service;
    }

    public static PlayerDataService getPlayerDataService() {
        return playerDataService;
    }
}
