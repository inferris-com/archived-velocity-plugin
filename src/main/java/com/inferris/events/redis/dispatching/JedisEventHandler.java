package com.inferris.events.redis.dispatching;

import com.inferris.player.service.PlayerDataService;
import com.inferris.player.manager.ManagerContainer;

public interface JedisEventHandler {
    void handle(PlayerDataService playerDataService, ManagerContainer managerContainer, String message, String senderId);
}