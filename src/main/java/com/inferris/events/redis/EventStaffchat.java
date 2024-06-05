package com.inferris.events.redis;

import com.inferris.Inferris;
import com.inferris.events.redis.dispatching.JedisEventHandler;
import com.inferris.messaging.StaffChatMessage;
import com.inferris.player.Channel;
import com.inferris.player.ChannelManager;
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

        Channel channel = staffChatMessage.getChannel();

        ChannelManager.sendStaffChatMessage(channel, staffChatMessage.getMessage(), ChannelManager.StaffChatMessageType.PLAYER, staffChatMessage.getPlayerUUID());
    }
}
