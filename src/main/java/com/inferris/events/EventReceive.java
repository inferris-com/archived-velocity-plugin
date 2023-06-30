package com.inferris.events;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.*;

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
            }
        }
    }
}