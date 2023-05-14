package com.inferris.player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class PlayerDataManager {
    private static PlayerDataManager instance;
    private final Cache<UUID,PlayerData> playerDataCache;

    public PlayerDataManager(){
        playerDataCache = CacheBuilder.newBuilder().build();
    }

    public static synchronized PlayerDataManager getInstance(){
        if(instance == null){
            instance = new PlayerDataManager();
        }
        return instance;
    }

    public PlayerData getPlayerData(ProxiedPlayer player){
        PlayerData playerData = playerDataCache.getIfPresent(player.getUniqueId());
        if(playerData == null){
            playerData = new PlayerData(player);
            playerDataCache.put(player.getUniqueId(), playerData);
        }
        return playerData;
    }

    public void invalidatePlayerData(ProxiedPlayer player){
        playerDataCache.invalidate(player.getUniqueId());
    }

    public Cache<UUID, PlayerData> getPlayerDataCache() {
        return playerDataCache;
    }
}
