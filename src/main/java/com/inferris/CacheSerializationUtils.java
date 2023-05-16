package com.inferris;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.inferris.util.BungeeChannels;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class CacheSerializationUtils {

    public void handlePlayerRegistryRequest(PluginMessageEvent event) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
        String message = in.readUTF();

        if (message.equalsIgnoreCase("request")) {
            Cache<UUID, String> cache = Initializer.getPlayerRegistryCache();


            ObjectMapper objectMapper = createObjectMapper();

            String cacheJson = objectMapper.writeValueAsString(cache);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(BungeeChannels.PLAYER_REGISTRY.getName());
            out.writeUTF("response");
            out.writeUTF(cacheJson);

            if(event.getReceiver() instanceof ProxiedPlayer player) {
                player.getServer().sendData(BungeeChannels.PLAYER_REGISTRY.getName(), out.toByteArray());

            }
        }
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

                .registerModule(new CaffeineModule()); // Register your custom Caffeine module here

        return objectMapper;
    }
}
