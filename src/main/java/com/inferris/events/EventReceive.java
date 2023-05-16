package com.inferris.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.inferris.CacheSerializationUtils;
import com.inferris.CaffeineModule;
import com.inferris.Inferris;
import com.inferris.Initializer;
import com.inferris.util.BungeeChannels;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.UUID;

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

                CacheSerializationUtils cacheSerializationUtils = new CacheSerializationUtils();
                cacheSerializationUtils.handlePlayerRegistryRequest(event);
            }
        }
    }
}