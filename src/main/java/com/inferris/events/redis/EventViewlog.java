package com.inferris.events.redis;

import com.inferris.Inferris;
import com.inferris.commands.CommandViewlogs;
import com.inferris.events.redis.dispatching.JedisEventHandler;
import com.inferris.messaging.ViewlogMessage;
import com.inferris.serialization.ViewlogSerializer;
import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;

/**
 * THis is the final destination of viewlog. /viewlogs pubs to -> Spigot, collects the needed logs, and -> pubs to Backend -> we receive here
 */

public class EventViewlog implements JedisEventHandler {
    private final CommandViewlogs viewLogCommand;
    public EventViewlog(CommandViewlogs viewLogCommand) {
        this.viewLogCommand = viewLogCommand;
    }

    @Override
    public void handle(String message, String senderId) {
        EventPayload payload = EventPayload.fromPayloadString(message);

        if (ServerStateManager.getCurrentState() == ServerState.DEBUG) {
            Inferris.getInstance().getLogger().warning("Executing processMessage: " + message); // Logging for debugging
        }

        // Deserialize logs into usable form
        ViewlogMessage viewlogMessage = ViewlogSerializer.deserialize(payload.getData());

        // Trigger received log method

        assert viewlogMessage != null;
        viewLogCommand.onLogReceived(viewlogMessage.getRequestedServer(), viewlogMessage.getUniqueRequestId(), payload.getUuid(), viewlogMessage.getChatLogMessages());
    }
}