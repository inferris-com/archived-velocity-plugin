package com.inferris.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.inferris.SerializationModule;
import com.inferris.player.PlayerData;
import com.inferris.player.friends.Friends;
import com.inferris.rank.Rank;

public class CacheSerializationUtils {

//    public void handlePlayerRegistryRequest(PluginMessageEvent event, ProxiedPlayer player) throws IOException {
//        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
//        String message = in.readUTF();
//
//        if (message.equalsIgnoreCase("request")) {
//
//            String cacheJson = serializeRegistry(RegistryManager.getInstance().getRegistry(player));
//
//            ByteArrayDataOutput out = ByteStreams.newDataOutput();
//            out.writeUTF(BungeeChannel.PLAYER_REGISTRY.getName());
//            out.writeUTF("response");
//            out.writeUTF(cacheJson);
//
//            if(event.getReceiver() instanceof ProxiedPlayer) {
//                player.getServer().sendData(BungeeChannel.PLAYER_REGISTRY.getName(), out.toByteArray());
//
//            }
//        }
//    } todo

    public static String serializeFriends(Friends friends) throws JsonProcessingException {
        ObjectMapper objectMapper = createObjectMapper(new SerializationModule());
        return objectMapper.writeValueAsString(friends);
    }

    public static Friends deserializeFriends(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = createObjectMapper(new SerializationModule());

        return objectMapper.readValue(json, Friends.class);
    }

    public static String serializePlayerData(PlayerData playerData) throws JsonProcessingException {
        ObjectMapper objectMapper = createObjectMapper(new SerializationModule());
        return objectMapper.writeValueAsString(playerData);
    }

    public static String serializeRank(Rank rank) throws JsonProcessingException {
        ObjectMapper objectMapper = createObjectMapper(new SerializationModule());
        return objectMapper.writeValueAsString(rank);
    }

    public static String serializeRegistry(Registry registry) throws JsonProcessingException {
        ObjectMapper objectMapper = createObjectMapper(new SerializationModule());
        return objectMapper.writeValueAsString(registry);
    }

    public static PlayerData deserializePlayerData(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = createObjectMapper(new SerializationModule());

        return objectMapper.readValue(json, PlayerData.class);
    }

    public static Registry deserializeRegistry(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = createObjectMapper(new SerializationModule());

        return objectMapper.readValue(json, Registry.class);
    }

    public static ObjectMapper createObjectMapper(Module module) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

                .registerModule(module) // Register your custom Caffeine module here
                .registerModule(new JavaTimeModule());

        return objectMapper;
    }
}
