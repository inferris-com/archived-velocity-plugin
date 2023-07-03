package com.inferris.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Branch;
import com.inferris.rank.RankRegistry;
import com.inferris.rank.RanksManager;
import com.inferris.server.BungeeChannel;
import com.inferris.server.Subchannel;
import com.inferris.util.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class EventReceive implements Listener {

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) throws IOException {
        String tag = event.getTag();
        String bungeeChannels = BungeeChannel.BUNGEECORD.getName();
        if (event.getReceiver() instanceof ProxiedPlayer player) {

            switch (tag) {
                case "inferris:staffchat" -> {
                    DataInputStream in = new DataInputStream((new ByteArrayInputStream(event.getData())));
                    PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
                    try {
                        String subchannel = in.readUTF();
                        String message = in.readUTF();

                        if (subchannel.equalsIgnoreCase(Subchannel.FORWARD.toLowerCase())) {

                            ProxyServer proxyServer = ProxyServer.getInstance();
                            RankRegistry rank = playerData.getByBranch();
                            TextComponent textComponent = new TextComponent(Tags.STAFF.getName(true)
                                    + rank.getPrefix(true) + player.getName() + ChatColor.RESET + ": " + message);

                            for (ProxiedPlayer proxiedPlayers : proxyServer.getPlayers()) {
                                if (PlayerDataManager.getInstance().getPlayerData(proxiedPlayers).getBranchValue(Branch.STAFF) >= 1) {
                                    proxiedPlayers.sendMessage(textComponent);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                case "inferris:report" -> {
                    DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
                    try {
                        String subchannel = in.readUTF();
                        String message = in.readUTF();

                        if (subchannel.equalsIgnoreCase(Subchannel.RESPONSE.toLowerCase())) {
                            try {
                                ObjectMapper objectMapper = new ObjectMapper();

                                List<String> chatMessages = objectMapper.readValue(message, new TypeReference<List<String>>(){});

                                for (String chatMessage : chatMessages) {
                                    int startIndex = chatMessage.indexOf("] ") + 2;
                                    String timestamp = chatMessage.substring(0, startIndex);
                                    String messageContent = chatMessage.substring(startIndex);

                                    player.sendMessage(timestamp + messageContent);
                                }
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}