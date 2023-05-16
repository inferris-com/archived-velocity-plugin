package com.inferris;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CaffeineModule extends SimpleModule {
    public CaffeineModule() {
        addSerializer(Cache.class, new CacheSerializer());
        addDeserializer(Cache.class, new CacheSerializer.CaffeineCacheDeserializer());
    }

    public static class CacheSerializer extends JsonSerializer<Cache> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void serialize(Cache cache, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            // Serialize the cache contents or any other relevant information
            jsonGenerator.disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);

            String serializedCache = objectMapper.writeValueAsString(cache.asMap());
            jsonGenerator.writeString(serializedCache);
        }

        private static class CaffeineCacheDeserializer extends JsonDeserializer<Cache<?, ?>> {
            @Override
            public Cache<?, ?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
                JsonNode node = mapper.readTree(jsonParser);

                // Perform deserialization and create the cache instance using the retrieved data
                Cache<UUID, String> cache = Caffeine.newBuilder().build(); // Replace with your actual cache initialization logic

                // Iterate over the JSON node to retrieve the cache entries
                for (Iterator<Map.Entry<String, JsonNode>> iterator = node.fields(); iterator.hasNext(); ) {
                    Map.Entry<String, JsonNode> entry = iterator.next();
                    UUID key = UUID.fromString(entry.getKey());
                    String value = entry.getValue().asText();
                    cache.put(key, value);
                }

                return cache;
            }
        }
    }
}
