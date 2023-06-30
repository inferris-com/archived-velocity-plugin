package com.inferris;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.inferris.player.Channels;
import com.inferris.player.Profile;
import com.inferris.player.coins.Coins;
import com.inferris.player.PlayerData;
import com.inferris.player.registry.Registry;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Rank;

import java.io.IOException;
import java.util.UUID;

public class SerializationModule extends SimpleModule {

    public SerializationModule() {
        addDeserializer(Registry.class, new RegistryDeserializer());
        addDeserializer(PlayerData.class, new PlayerDataDeserializer());

        addSerializer(Registry.class, new RegistrySerializer());
        addSerializer(PlayerData.class, new PlayerDataSerializer());
    }

    public static class PlayerDataSerializer extends JsonSerializer<PlayerData> {
       // private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void serialize(PlayerData playerData, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            // Serialize the cache contents or any other relevant information
            ObjectMapper objectMapper = (ObjectMapper) jsonGenerator.getCodec();

            objectMapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY).disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("registry", playerData.getRegistry());
            jsonGenerator.writeObjectField("rank", playerData.getRank());
            jsonGenerator.writeObjectField("profile", playerData.getProfile());
            jsonGenerator.writeObjectField("coins", playerData.getCoins());
            jsonGenerator.writeObjectField("channel", playerData.getChannel());
            jsonGenerator.writeObjectField("vanishState", playerData.getVanishState());
            jsonGenerator.writeEndObject();


//            String serializedCache = objectMapper.writeValueAsString(playerData);
//            //jsonGenerator.writeString(serializedCache);   DONT USE
//            jsonGenerator.writeRawValue(serializedCache);
        }
    }
    public static class PlayerDataDeserializer extends JsonDeserializer<PlayerData> {
        @Override
        public PlayerData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
            JsonNode registryNode = objectMapper.readTree(jsonParser);

            Registry registry = objectMapper.treeToValue(registryNode.get("registry"), Registry.class);
            Rank rank = objectMapper.treeToValue(registryNode.get("rank"), Rank.class);
            Profile profile = objectMapper.treeToValue(registryNode.get("profile"), Profile.class);
            Coins coins = objectMapper.treeToValue(registryNode.get("coins"), Coins.class);
            Channels channel = objectMapper.treeToValue(registryNode.get("channel"), Channels.class);
            VanishState vanishState = objectMapper.treeToValue(registryNode.get("vanishState"), VanishState.class);


            return new PlayerData(registry, rank, profile, coins, channel, vanishState);
        }
    }

    public static class RegistrySerializer extends JsonSerializer<Registry> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void serialize(Registry registry, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            // Serialize the cache contents or any other relevant information
            objectMapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);

            String serializedCache = objectMapper.writeValueAsString(registry);
            //jsonGenerator.writeString(serializedCache);
            jsonGenerator.writeRawValue(serializedCache);
        }
    }

    public static class RegistryDeserializer extends JsonDeserializer<Registry> {
        @Override
        public Registry deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            ObjectMapper objectMapper = (ObjectMapper) jsonParser.getCodec();
            JsonNode registryNode = objectMapper.readTree(jsonParser);

            UUID uuid = UUID.fromString(registryNode.get("uuid").asText());
            String username = registryNode.get("username").asText();

            return new Registry(uuid, username);
        }
    }
}