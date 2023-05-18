package com.inferris.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerCommandCache {
    private final Cache<UUID, Integer> cache;

    public PlayerCommandCache() {
        // Configure the cache
        cache = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .build();
    }

    public Cache<UUID, Integer> getCache() {
        return cache;
    }

    public int getCommandCount(UUID playerUUID) {
        return cache.get(playerUUID, uuid -> 0);
    }

    public void incrementCommandCount(UUID playerUUID) {
        cache.put(playerUUID, getCommandCount(playerUUID) + 1);
    }
}
