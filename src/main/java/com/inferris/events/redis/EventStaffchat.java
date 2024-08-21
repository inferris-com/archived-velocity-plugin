package com.inferris.events.redis;

import com.inferris.Inferris;
import com.inferris.events.redis.dispatching.JedisEventHandler;
import com.inferris.messaging.StaffChatMessage;
import com.inferris.player.channel.Channel;
import com.inferris.player.channel.ChannelManager;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.service.ManagerContainer;
import com.inferris.serialization.StaffChatSerializer;
import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;
import net.md_5.bungee.api.ProxyServer;

public class EventStaffchat implements JedisEventHandler {
    @Override
    public void handle(PlayerDataService playerDataService, ManagerContainer managerContainer, String message, String senderId) {
        EventPayload payload = EventPayload.fromPayloadString(message);
        ProxyServer.getInstance().getLogger().severe("Payload: " + payload.toPayloadString());
        if (ProxyServer.getInstance().getPlayer(payload.getUuid()) == null || senderId.equals(Inferris.getInstanceId())) {
            return;
        }

        if (ServerStateManager.getCurrentState() == ServerState.DEBUG)
            Inferris.getInstance().getLogger().severe("Triggered");

        StaffChatMessage staffChatMessage = StaffChatSerializer.deserialize(payload.getData());
        assert staffChatMessage != null;

        Channel channel = staffChatMessage.getChannel();
        ChannelManager channelManager = managerContainer.getChannelManager();
        channelManager.sendStaffChatMessage(channel, staffChatMessage.getMessage(), ChannelManager.StaffChatMessageType.PLAYER, staffChatMessage.getPlayerUUID());
    }
}
