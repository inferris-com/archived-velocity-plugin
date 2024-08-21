package com.inferris.events.redis.dispatching;

import com.google.inject.Inject;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.service.ManagerContainer;

import java.util.HashMap;
import java.util.Map;

public class JedisEventDispatcher {
    private final Map<String, JedisEventHandler> eventHandlers = new HashMap<>();
    private final PlayerDataService playerDataService;
    private final ManagerContainer managerContainer;

    @Inject
    public JedisEventDispatcher(PlayerDataService playerDataService, ManagerContainer managerContainer) {
        this.playerDataService = playerDataService;
        this.managerContainer = managerContainer;
    }


    public void registerHandler(String channel, JedisEventHandler handler) {
        eventHandlers.put(channel, handler);
    }

    public void dispatch(String channel, String message, String senderId) {
        JedisEventHandler handler = eventHandlers.get(channel);
        if (handler != null) {
            handler.handle(playerDataService, managerContainer, message, senderId);
        } else {
            System.err.println("No handler registered for channel: " + channel);
        }
    }
}