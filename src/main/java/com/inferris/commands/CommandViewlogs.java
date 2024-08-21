package com.inferris.commands;

import com.inferris.Inferris;
import com.inferris.events.redis.EventPayload;
import com.inferris.events.redis.PlayerAction;
import com.inferris.messaging.ViewlogMessage;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.serialization.ViewlogSerializer;
import com.inferris.server.*;
import com.inferris.server.jedis.JedisChannel;
import com.inferris.util.ServerUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * The CommandViewlogs class represents a command used to view logs of a specific server in a game proxy.
 * It allows players to request and view the logs of a particular server.
 * The command can be executed by players to retrieve logs for analysis or debugging purposes.
 * The requested server is specified as a command argument.
 * If the server is valid, the command sends a Bungee message to retrieve the logs for the requested server.
 *
 * @since 1.0
 */

public class CommandViewlogs extends Command {
    private final PlayerDataService playerDataService;
    public CommandViewlogs(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    private final Map<UUID, Consumer<String>> callbacks = new ConcurrentHashMap<>();


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            if (args.length != 1) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /viewlogs <server>"));
                return;
            }

            // Generate callback ID
            UUID requestId = UUID.randomUUID();
            Inferris.getInstance().getLogger().info("Generated requestId: " + requestId);

            callbacks.put(requestId, logData -> {
                sender.sendMessage(new TextComponent(logData));
                callbacks.remove(requestId);
                Inferris.getInstance().getLogger().info("Callback executed and removed for requestId: " + requestId);
            });

            String requestedServer = args[0].toLowerCase();
            if (!isValidServer(requestedServer)) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Error: Invalid server!"));
                return;
            }

            try (Jedis jedis = Inferris.getJedisPool().getResource()) {
                ViewlogMessage viewlogMessage = new ViewlogMessage(requestId, player.getUniqueId(), requestedServer, null, 0L, null);

                Inferris.getInstance().getLogger().info("[CommandViewlogs] Publishing payload: " + ViewlogSerializer.serialize(viewlogMessage));

                // remember: diff payloads
                jedis.publish(JedisChannel.VIEW_LOGS_PROXY_TO_SPIGOT.getChannelName(), new EventPayload(player.getUniqueId(),
                        PlayerAction.VIEW_LOGS,
                        ViewlogSerializer.serialize(viewlogMessage),
                        Inferris.getInstanceId()).toPayloadString());

                ProxyServer.getInstance().getScheduler().schedule(Inferris.getInstance(), () -> {
                    if (callbacks.containsKey(requestId)) {
                        sender.sendMessage(new TextComponent(ChatColor.RED + "Request timed out - invalid callback key. Inform an administrator or developer immediately," +
                                " as this may conclude Redis is down, or the publication channels are not working properly which may impact player data integrity."));
                        callbacks.remove(requestId);
                    }
                }, 5L, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            PlayerContext playerContext = new PlayerContext(player.getUniqueId(), playerDataService);
            return playerContext.getRank().getBranchValue(Branch.STAFF) >= 2;
        }
        // Allow console to execute the command
        return sender.getName().equalsIgnoreCase("CONSOLE");
    }

    /**
     * Checks if the specified server is valid.
     * It compares the server name with the valid servers defined in the Servers enum.
     *
     * @param server the server name to validate
     * @return true if the server is valid, false otherwise
     */

    private boolean isValidServer(String server) {
        for (Server validServer : Server.values()) {
            if (validServer.toString().equalsIgnoreCase(server.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public void onLogReceived(String requestedServer, UUID requestId, UUID playerUuid, List<String> logData) {
        ServerUtil.log("Callback size: " + callbacks.size(), Level.INFO, ServerState.DEBUG);
        if (callbacks.containsKey(requestId)) {
            Inferris.getInstance().getLogger().warning("Contains key: onLogReceived");
            Consumer<String> callback = callbacks.get(requestId);

            ProxyServer.getInstance().getScheduler().runAsync(Inferris.getInstance(), () -> {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUuid);
                if (player == null) {
                    Inferris.getInstance().getLogger().info("Player with UUID " + playerUuid + " not found.");
                    return;
                }

                StringBuilder formattedLogs = new StringBuilder();
                if (logData.isEmpty()) {
                    formattedLogs.append(ChatColor.RED).append("No chat log messages available.");
                } else {
                    formattedLogs.append(ChatColor.AQUA).append("Chat logs for ").append(requestedServer).append(":").append("\n");
                    for (String chatMessage : logData) {
                        formattedLogs.append(formatChatMessage(chatMessage)).append("\n");
                    }
                }

                callback.accept(formattedLogs.toString());
            });
        } else {
            Inferris.getInstance().getLogger().severe("Does not contain key: onLogReceived");
        }
    }

    private String formatChatMessage(String chatMessage) {
        // Example input: "[Chat] [01:22:38 UTC] Username: Any message here"

        // Extract the prefix "[Chat]"
        String prefix = "[Chat]";
        int prefixLength = prefix.length();

        // Extract the timestamp
        int timestampEndIndex = chatMessage.indexOf(" UTC]") + 5; // Length of " UTC]" is 5 characters
        String timestamp = chatMessage.substring(prefixLength, timestampEndIndex).trim();

        // Extract the username and message content
        String remainingContent = chatMessage.substring(timestampEndIndex + 1); // Skip the space after the timestamp
        int usernameEndIndex = remainingContent.indexOf(": ");
        String username = remainingContent.substring(0, usernameEndIndex);
        String userMessage = remainingContent.substring(usernameEndIndex + 2);

        // Format each part with the specified colors
        String formattedPrefix = ChatColor.RED + prefix;
        String formattedTimestamp = ChatColor.GRAY + timestamp;
        String formattedUsername = ChatColor.WHITE + username;
        String formattedMessage = ChatColor.YELLOW + userMessage;

        // Combine everything
        return formattedPrefix + " " + formattedTimestamp + " " + formattedUsername + ": " + formattedMessage;
    }
}
