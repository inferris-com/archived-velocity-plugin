package com.inferris.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inferris.*;
import com.inferris.player.Channels;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.registry.RegistryManager;
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
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.io.*;

public class EventReceive implements Listener {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) throws IOException {
        String tag = event.getTag();
        String bungeeChannels = BungeeChannel.BUNGEECORD.getName();
        if (event.getReceiver() instanceof ProxiedPlayer player) {

            switch (tag) {
                case "inferris:staffchat" -> {
                    DataInputStream in = new DataInputStream((new ByteArrayInputStream(event.getData())));
                    try {
                        String subchannel = in.readUTF();
                        String message = in.readUTF();

                        if (subchannel.equalsIgnoreCase(Subchannel.FORWARD.toLowerCase())) {

                            ProxyServer proxyServer = ProxyServer.getInstance();
                            RankRegistry rank = PlayerDataManager.getInstance().getPlayerData(player).getByBranch();
                            TextComponent textComponent = new TextComponent(Tags.STAFF.getName(true)
                                    + rank.getPrefix(true) + player.getName() + ChatColor.RESET + ": " + message);

                            for (ProxiedPlayer proxiedPlayers : proxyServer.getPlayers()) {
                                if (RanksManager.getInstance().getRank(proxiedPlayers).getBranchID(Branch.STAFF) >= 1) {
                                    proxiedPlayers.sendMessage(textComponent);
                                }
                            }
                        }

                        if (subchannel.equalsIgnoreCase(Subchannel.REQUEST.toLowerCase())) {
                            Channels channel = Channels.valueOf(message.toUpperCase());
                            Configuration configuration = Inferris.getPlayersConfiguration();
                            configuration.getSection("players").set(player.getUniqueId() + ".channel", channel.toString());
                            new ConfigUtils().saveConfiguration(Inferris.getPlayersFile(), configuration);
                            new ConfigUtils().reloadConfiguration(ConfigUtils.Types.PLAYERS);

                            Inferris.getInstance().getLogger().severe("Channel set to " + channel);

                            RegistryManager.getInstance().getRegistry(player).setChannel(channel);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                case "inferris:player_registry" -> {
                    DataInputStream in = new DataInputStream((new ByteArrayInputStream(event.getData())));
                    String message = in.readUTF();

                    CacheSerializationUtils cacheSerializationUtils = new CacheSerializationUtils();
                    cacheSerializationUtils.handlePlayerRegistryRequest(event, player);
                }
            }
        }
    }
}