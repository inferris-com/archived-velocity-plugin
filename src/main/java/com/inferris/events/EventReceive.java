package com.inferris.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.inferris.util.BungeeChannels;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;

public class EventReceive implements Listener {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) throws IOException {
        String tag = event.getTag();
        String bungeeChannels = BungeeChannels.BUNGEECORD.getName();

        switch (tag) {

            case "inferris:staffchat" -> {
                DataInputStream in = new DataInputStream((new ByteArrayInputStream(event.getData())));
                try {
                    String message = in.readUTF();
                    ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case "inferris:player_registry" -> {
                DataInputStream in = new DataInputStream((new ByteArrayInputStream(event.getData())));
                String message = in.readUTF();
                if (message.equalsIgnoreCase("request")) {

                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    ProxyServer.getInstance().getLogger().warning("Received " + message);

                    out.writeUTF(BungeeChannels.PLAYER_REGISTRY.getName());
                    out.writeUTF("response");
                    out.writeInt(36);

                    if(event.getReceiver() instanceof ProxiedPlayer player) {
                        player.getServer().sendData(BungeeChannels.PLAYER_REGISTRY.getName(), out.toByteArray());
                    }
                }
            }
        }
    }
}