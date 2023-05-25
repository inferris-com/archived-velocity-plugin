package com.inferris;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.inferris.player.Channels;
import com.inferris.player.registry.Registry;
import com.inferris.player.vanish.VanishState;

import java.io.IOException;
import java.util.UUID;

public class SerializationModule extends SimpleModule {

    public SerializationModule() {
        addDeserializer(Registry.class, new RegistryDeserializer());
        addSerializer(Registry.class, new RegistrySerializer());
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
            Channels channel = Channels.valueOf(registryNode.get("channel").asText());
            VanishState vanishState = VanishState.valueOf(registryNode.get("vanishState").asText());

            return new Registry(uuid, username, channel, vanishState);
        }
    }
}