package com.inferris.events.redis;

import com.inferris.events.redis.dispatching.JedisEventHandler;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.manager.ManagerContainer;

public class EventPlayerFlex implements JedisEventHandler {
    @Override
    public void handle(PlayerDataService playerDataService, ManagerContainer managerContainer, String message, String senderId) {
    }
}
