package com.inferris.player;

import com.inferris.player.service.PlayerDataService;

public class ServiceLocator {
    private static PlayerDataService playerDataService;

    public static void setPlayerDataService(PlayerDataService service) {
        playerDataService = service;
    }

    public static PlayerDataService getPlayerDataService() {
        return playerDataService;
    }
}
