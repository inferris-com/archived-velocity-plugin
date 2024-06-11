package com.inferris.server;

import com.inferris.player.PlayerDataService;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlayerSessionManager {
    private static final ConcurrentMap<UUID, Boolean> hasJoinedBeforeMap = new ConcurrentHashMap<>();

    public static boolean hasPlayerJoinedBefore(UUID uuid, PlayerDataService playerDataService) {
        return hasJoinedBeforeMap.computeIfAbsent(uuid, playerDataService::hasJoinedBefore);
    }

    public static void clearPlayerSession(UUID uuid) {
        hasJoinedBeforeMap.remove(uuid);
    }
}
