package com.inferris;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inferris.rank.Rank;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class RegistryManager {
    private static RegistryManager instance;

    private static Cache<UUID, Registry> playerRegistryCache;

    private RegistryManager() {
        playerRegistryCache = Caffeine.newBuilder().build();
    }

    public static RegistryManager getInstance() {
        if (instance == null) {
            return instance = new RegistryManager();
        }
        return instance;
    }

    public Registry getRegistry(UUID uuid) {
        if (playerRegistryCache.getIfPresent(uuid) != null) {
            return playerRegistryCache.getIfPresent(uuid);
        }
        return null;
    }

    public static Cache<UUID, Registry> getPlayerRegistryCache() {
        return playerRegistryCache;
    }
}