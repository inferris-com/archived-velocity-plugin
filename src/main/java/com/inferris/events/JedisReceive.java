package com.inferris.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inferris.Inferris;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.server.*;
import com.inferris.server.jedis.JedisChannels;
import com.inferris.util.SerializationUtils;
import com.inferris.util.ServerUtil;
import com.inferris.server.Tags;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.UUID;

public class JedisReceive extends JedisPubSub {

    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void onMessage(String channel, String message) {
        Inferris.getInstance().getLogger().severe("Triggered EventReceive");

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

        if(channel.equalsIgnoreCase(JedisChannels.VIEW_LOGS_SPIGOT_TO_PROXY.getChannelName())){

            String[] parts = message.split(":");
            UUID uuid = UUID.fromString(parts[0]);
            String json = message.substring(parts[0].length() + 1);

            PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(uuid);
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerData.getUuid());

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<String> chatMessages = objectMapper.readValue(json, new TypeReference<>() {
                });

                if (chatMessages.isEmpty()) {
                    player.sendMessage(new TextComponent(ChatColor.RED + "No chat log messages available."));
                    return;
                }

                for (String chatMessage : chatMessages) {
                    int startIndex = chatMessage.indexOf("] ") + 2;
                    String timestamp = chatMessage.substring(0, startIndex);
                    String messageContent = chatMessage.substring(startIndex);

                    player.sendMessage(new TextComponent(timestamp + messageContent));
                }

                if(ServerStateManager.getCurrentState() == ServerState.DEBUG) {
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
