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

public class CacheSerializationUtils {

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

    public static PlayerData deserializePlayerData(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = createObjectMapper(new SerializationModule());

        return objectMapper.readValue(json, PlayerData.class);
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
