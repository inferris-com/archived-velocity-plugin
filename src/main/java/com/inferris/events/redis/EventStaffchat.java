package com.inferris.events.redis;

import com.inferris.Inferris;
import com.inferris.events.redis.dispatching.JedisEventHandler;
import com.inferris.messaging.StaffChatMessage;
import com.inferris.serialization.StaffChatSerializer;
import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;
import com.inferris.util.ChatUtil;

public class EventStaffchat implements JedisEventHandler {
    @Override
    public void handle(String message) {

        if (ServerStateManager.getCurrentState() == ServerState.DEBUG)
            Inferris.getInstance().getLogger().severe("Triggered");

        StaffChatMessage staffChatMessage = StaffChatSerializer.deserialize(message);
        assert staffChatMessage != null;

        ChatUtil.sendStaffChatMessage(staffChatMessage.getMessage(), ChatUtil.StaffChatMessageType.PLAYER, staffChatMessage.getPlayerUUID());
    }
}
