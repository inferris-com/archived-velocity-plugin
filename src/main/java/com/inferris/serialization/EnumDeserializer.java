package com.inferris.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class EnumDeserializer extends JsonDeserializer<Enum<?>> {
    @Override
    public Enum<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);
        String type = node.get("type").asText();
        String name = node.get("name").asText();

        try {
            Class<Enum> enumClass = (Class<Enum>) Class.forName(type);
            return Enum.valueOf(enumClass, name);
        } catch (ClassNotFoundException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }
}