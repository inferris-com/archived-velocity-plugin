/*
 * Copyright (c) 2024. Inferris.
 * All rights reserved.
 */

package com.inferris.player.manager;

import com.google.inject.Inject;
import com.inferris.player.channel.ChannelManager;
import com.inferris.player.friends.FriendsManager;

public class ManagerContainer {
    private final PlayerDataManager playerDataManager;
    private final ChannelManager channelManager;
    private final CoinsManager coinsManager;
    private final FriendsManager friendsManager;
    private final RanksManager ranksManager;

    @Inject
    public ManagerContainer(PlayerDataManager playerDataManager, RanksManager ranksManager, ChannelManager channelManager, CoinsManager coinsManager, FriendsManager friendsManager) {
        this.playerDataManager = playerDataManager;
        this.ranksManager = ranksManager;
        this.channelManager = channelManager;
        this.coinsManager = coinsManager;
        this.friendsManager = friendsManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public RanksManager getRanksManager() {
        return ranksManager;
    }

    // Provide methods to access or operate on the managers
    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public CoinsManager getCoinsManager() {
        return coinsManager;
    }

    public FriendsManager getFriendsManager() {
        return friendsManager;
    }
}
