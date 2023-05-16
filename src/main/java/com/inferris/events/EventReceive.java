package com.inferris.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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
                if (message.equalsIgnoreCase("request")) {

                    ObjectMapper mapper = new ObjectMapper();
                    CaffeineModule caffeineModule = new CaffeineModule();
                    mapper.registerModule(caffeineModule);
                    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

                    Cache<UUID, String> cache = Initializer.getPlayerRegistryCache(); // Your Caffeine cache
                    String cacheJson;
                    try{
                        cacheJson = mapper.writeValueAsString(cache);
                    }catch(JsonProcessingException e){
                        e.printStackTrace();
                        return;
                    }


                    ByteArrayDataOutput out = ByteStreams.newDataOutput();

                    ProxyServer.getInstance().getLogger().warning("Received " + message);

                    out.writeUTF(BungeeChannels.PLAYER_REGISTRY.getName());
                    out.writeUTF("response");
                    out.writeUTF(cacheJson);

                    if(event.getReceiver() instanceof ProxiedPlayer player) {
                        Inferris.getInstance().getLogger().warning(cache.getIfPresent(player.getUniqueId()));

                        player.getServer().sendData(BungeeChannels.PLAYER_REGISTRY.getName(), out.toByteArray());
                    }
                }
            }
        }
    }
}