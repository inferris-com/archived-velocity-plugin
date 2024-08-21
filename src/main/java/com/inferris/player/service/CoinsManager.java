package com.inferris.player.service;

import com.google.inject.Inject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoinsManager {
    private static CoinsManager instance;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final PlayerDataService playerDataService;

    @Deprecated
    @Inject
    public CoinsManager(PlayerDataService playerDataService){
        this.playerDataService = playerDataService;
    }
}