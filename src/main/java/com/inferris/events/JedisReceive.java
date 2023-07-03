package com.inferris.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inferris.Inferris;
import com.inferris.player.PlayerDataManager;
import com.inferris.server.JedisChannels;
import com.inferris.server.ReportPayload;
import com.inferris.util.Tags;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class JedisReceive extends JedisPubSub {

    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void onMessage(String channel, String message) {
        Inferris.getInstance().getLogger().severe("Triggered JedisPlayerDataEvent");
        if (channel.equalsIgnoreCase(JedisChannels.SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE.name())) {
            Inferris.getInstance().getLogger().severe("Yup");

            try{
                ReportPayload reportPayload = objectMapper.readValue(message, ReportPayload.class);

                handleReportPayload(reportPayload);

            }catch(JsonProcessingException e){
                e.printStackTrace();
            }
        }
    }

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
