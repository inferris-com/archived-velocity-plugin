/*
 * Copyright (c) 2024. Inferris.
 * All rights reserved.
 */

package com.inferris;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.inferris.player.PlayerData;
import com.inferris.player.channel.ChannelManager;
import com.inferris.player.friends.FriendsManager;
import com.inferris.player.manager.CoinsManager;
import com.inferris.player.manager.ManagerContainer;
import com.inferris.player.manager.PlayerDataManager;
import com.inferris.player.manager.RanksManager;
import com.inferris.player.repository.PlayerDataRepository;
import com.inferris.player.service.*;
import com.inferris.rank.Permissions;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PlayerDataService.class).to(PlayerDataServiceImpl.class).in(Singleton.class);
        bind(ManagerContainer.class).in(Singleton.class);
        bind(PlayerDataRepository.class).in(Singleton.class);
        bind(Permissions.class).in(Singleton.class);

        bind(PlayerDataManager.class).in(Singleton.class);
        bind(RanksManager.class).in(Singleton.class);

        bind(FriendsManager.class).in(Singleton.class);
        bind(CoinsManager.class).in(Singleton.class);
        bind(ChannelManager.class).in(Singleton.class);

        bind(ExecutorService.class).toInstance(Executors.newCachedThreadPool());
        bind(new TypeLiteral<Cache<UUID, PlayerData>>() {}).toInstance(Caffeine.newBuilder().build());
    }
}
