package com.inferris.events.redis;

import com.inferris.Inferris;
import com.inferris.events.redis.dispatching.JedisEventHandler;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.manager.ManagerContainer;
import com.inferris.server.jedis.JedisChannel;
import com.inferris.server.jedis.JedisHelper;

public class EventGenericFlex implements JedisEventHandler {
    @Override
    public void handle(PlayerDataService playerDataService, ManagerContainer managerContainer, String message, String senderId) {
        EventPayload payload = EventPayload.fromPayloadString(message);
        String data = payload.getData();

        if (senderId.equals(Inferris.getInstanceId())) {
            return;
        }
        switch ((GenericAction) payload.getAction()) {
            case INFORMATION -> {
                if (data.equals("fetchOnlineVisiblePlayers")) {
                    JedisHelper.publish(JedisChannel.GENERIC_FLEX_EVENT, new EventPayload(payload.getUuid(),
                            GenericAction.INFORMATION,
                            "fetchOnlineVisiblePlayers:return:" + Inferris.getInstance().getVisibleOnlinePlayers(), Inferris.getInstanceId()).toPayloadString());
                }
            }
        }
    }
}
