package com.inferris.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class EnumSerializer extends JsonSerializer<Enum<?>> {
    @Override
    public void serialize(Enum<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", value.getClass().getName());
        gen.writeStringField("name", value.name());
        gen.writeEndObject();
    }
}
