package com.inferris.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.inferris.player.PlayerData;
import com.inferris.player.friends.Friends;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class SerializationModule extends SimpleModule {

    public SerializationModule() {
        //addDeserializer(Registry.class, new RegistryDeserializer());
        addDeserializer(PlayerData.class, new PlayerDataDeserializer());
        addDeserializer(Friends.class, new FriendsDeserializer());

        //addSerializer(Registry.class, new RegistrySerializer());
        addSerializer(PlayerData.class, new PlayerDataSerializer());
        addSerializer(Friends.class, new FriendsSerializer());
    }

    public static class FriendsSerializer extends JsonSerializer<Friends> {
        @Override
        public void serialize(Friends friends, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            ObjectMapper objectMapper = (ObjectMapper) jsonGenerator.getCodec();

            objectMapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY).disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("friendsList", friends.getFriendsList());
            jsonGenerator.writeObjectField("pendingFriendsList", friends.getPendingFriendsList());
            jsonGenerator.writeEndObject();
        }
    }

    public static class FriendsDeserializer extends JsonDeserializer<Friends> {
        @Override
        public Friends deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
            JsonNode friendsNode = objectMapper.readTree(jsonParser);

            List<UUID> friendsList = objectMapper.readValue(friendsNode.get("friendsList").traverse(), new TypeReference<List<UUID>>() {});
            List<UUID> pendingFriendsList = objectMapper.readValue(friendsNode.get("pendingFriendsList").traverse(), new TypeReference<List<UUID>>() {});

            Friends friends = new Friends();
            friends.getFriendsList().addAll(friendsList);
            friends.getPendingFriendsList().addAll(pendingFriendsList);

            return friends;
        }
    }
}