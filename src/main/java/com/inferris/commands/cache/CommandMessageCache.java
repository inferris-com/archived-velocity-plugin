package com.inferris.commands.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandMessageCache {
    private final Cache<UUID, UUID> cache;
    private final ProxiedPlayer receiver;
    private final ProxiedPlayer sender;

    public CommandMessageCache(ProxiedPlayer receiver, ProxiedPlayer sender, Long time, TimeUnit timeUnit) {
        // Configure the cache
        cache = Caffeine.newBuilder()
                .expireAfterAccess(time, timeUnit)
                .build();

        this.receiver = receiver;
        this.sender = sender;
    }

    public void add(){
        cache.put(receiver.getUniqueId(), sender.getUniqueId());
    }

    public Cache<UUID, UUID> getCache() {
        return cache;
    }
}
