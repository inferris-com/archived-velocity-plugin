package com.inferris;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

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

    public Registry getRegistry(ProxiedPlayer player) {
        if(playerRegistryCache.getIfPresent(player.getUniqueId()) != null){
            return playerRegistryCache.getIfPresent(player.getUniqueId());
        }else{
            return addToRegistry(player);
        }
    }

    public Registry addToRegistry(ProxiedPlayer player){
        Registry registry = new Registry(player.getUniqueId(), player.getName(), Channels.NONE);
        playerRegistryCache.put(player.getUniqueId(), registry);
        return registry;
    }

    public static Cache<UUID, Registry> getPlayerRegistryCache() {
        return playerRegistryCache;
    }
}