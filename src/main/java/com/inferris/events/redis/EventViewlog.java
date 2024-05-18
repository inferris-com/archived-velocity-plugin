package com.inferris.events.redis;

import com.inferris.Inferris;
import com.inferris.commands.CommandViewlogs;
import com.inferris.events.redis.dispatching.JedisEventHandler;
import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class EventViewlog implements JedisEventHandler {
    private final CommandViewlogs viewLogCommand;

    public EventViewlog(CommandViewlogs viewLogCommand) {
        this.viewLogCommand = viewLogCommand;
    }

    @Override
    public void handle(String message) {
        if (ServerStateManager.getCurrentState() == ServerState.DEBUG) {
            Inferris.getInstance().getLogger().warning("Executing processMessage: " + message); // Logging for debugging
        }

        String[] parts = message.split(":", 4); // Split into four parts
        String requestedServer = parts[0];
        UUID uuid = UUID.fromString(parts[1]);
        UUID requestUuid = UUID.fromString(parts[2]);
        String json = parts[3];

        viewLogCommand.onLogReceived(requestedServer, requestUuid, uuid, json);

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
    }
}