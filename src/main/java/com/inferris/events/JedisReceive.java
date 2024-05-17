package com.inferris.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inferris.Inferris;
import com.inferris.messaging.StaffChatMessage;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Branch;
import com.inferris.rank.RankRegistry;
import com.inferris.serialization.StaffChatSerializer;
import com.inferris.server.*;
import com.inferris.server.jedis.JedisChannels;
import com.inferris.util.SerializationUtils;
import com.inferris.util.ServerUtil;
import com.inferris.server.Tags;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.UUID;

public class JedisReceive extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {

        if (channel.equalsIgnoreCase(JedisChannels.PLAYERDATA_UPDATE.getChannelName())) {
            Inferris.getInstance().getLogger().severe("Proxy received update");
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(UUID.fromString(message));
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
            PlayerDataManager.getInstance().updateAllData(player, playerData);
        }

        if (channel.equalsIgnoreCase(JedisChannels.SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE.getChannelName())) {
            Inferris.getInstance().getLogger().severe("Spigot updated Proxy cache");

            try {
                PlayerData playerData = SerializationUtils.deserializePlayerData(message);
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerData.getUuid());
                PlayerDataManager.getInstance().updateAllData(player, playerData);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        if (channel.equals(JedisChannels.STAFFCHAT.getChannelName())) {
            Inferris.getInstance().getLogger().severe("Triggered");
            StaffChatMessage staffChatMessage = StaffChatSerializer.deserialize(message);
            assert staffChatMessage != null;

            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(staffChatMessage.getPlayerUUID());
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerData.getUuid());
            ProxyServer proxyServer = ProxyServer.getInstance();
            RankRegistry rank = playerData.getByBranch();

            BaseComponent[] textComponent = TextComponent.fromLegacyText(Tags.STAFF.getName(true)
                    + rank.getPrefix(true) + playerData.getNameColor() + player.getName() + ChatColor.RESET + ": " + staffChatMessage.getMessage());

            for (ProxiedPlayer proxiedPlayers : proxyServer.getPlayers()) {
                if (PlayerDataManager.getInstance().getPlayerData(proxiedPlayers).getBranchValue(Branch.STAFF) >= 1) {
                    proxiedPlayers.sendMessage(textComponent);
                }
            }
        }

        if (channel.equalsIgnoreCase(JedisChannels.VIEW_LOGS_SPIGOT_TO_PROXY.getChannelName())) {
            Inferris.getInstance().getLogger().warning("JedisReceive triggered"); // Logging for debugging
            Inferris.getInstance().getLogger().warning("Executing processMessage: " + message); // Logging for debugging

            String[] parts = message.split(":");
            UUID uuid = UUID.fromString(parts[0]);
            String json = message.substring(parts[0].length() + 1);

            PlayerData playerData = PlayerDataManager.getInstance().getRedisData(uuid);
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerData.getUuid());

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<String> chatMessages = objectMapper.readValue(json, new TypeReference<List<String>>() {
                });
                Inferris.getInstance().getLogger().warning("Executing processMessage: " + chatMessages.size());

                if (chatMessages.isEmpty()) {
                    player.sendMessage(new TextComponent(ChatColor.RED + "No chat log messages available."));
                } else {
                    player.sendMessage("--------------------------------------------------------------");
                    for (String chatMessage : chatMessages) {
                        int startIndex = chatMessage.indexOf("] ") + 2;
                        String timestamp = chatMessage.substring(0, startIndex);
                        String messageContent = chatMessage.substring(startIndex);
                        player.sendMessage(new TextComponent(timestamp + messageContent));
                    }
                }

                if (ServerStateManager.getCurrentState() == ServerState.DEBUG) {
                    Inferris.getInstance().getLogger().severe("================================");
                    Inferris.getInstance().getLogger().severe("Event has been received");
                    Inferris.getInstance().getLogger().severe("================================");
                    ServerUtil.broadcastMessage(json);
                    ServerUtil.broadcastMessage(String.valueOf(uuid));
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Deprecated
    private void handleReportPayload(ReportPayload reportPayload) {

        for (ProxiedPlayer staffPlayer : ProxyServer.getInstance().getPlayers()) {
            // Check if the player is a staff member
            if (PlayerDataManager.getInstance().getPlayerData(staffPlayer).isStaff()) {
                staffPlayer.sendMessage(new TextComponent(Tags.STAFF.getName(true) + ChatColor.RED + "New chat report!"));
                staffPlayer.sendMessage(new TextComponent(""));

                // Send the report information to the staff player
                staffPlayer.sendMessage(new TextComponent(ChatColor.GRAY + "Reported Player: " + ChatColor.YELLOW + reportPayload.getReported()));
                staffPlayer.sendMessage(new TextComponent(ChatColor.GRAY + "Reported by: " + ChatColor.RESET + reportPayload.getSender()));
                staffPlayer.sendMessage(new TextComponent(""));

                staffPlayer.sendMessage(new TextComponent("Reason: " + ChatColor.YELLOW + reportPayload.getReason()));
                staffPlayer.sendMessage(new TextComponent("Server: " + ChatColor.YELLOW + reportPayload.getServer()));
            }
        }
    }
}
