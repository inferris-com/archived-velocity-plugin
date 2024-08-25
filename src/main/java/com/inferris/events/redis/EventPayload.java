package com.inferris.events.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.inferris.serialization.EnumDeserializer;
import com.inferris.serialization.EnumSerializer;

import java.util.UUID;

public class EventPayload {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private UUID uuid;
    @JsonSerialize(using = EnumSerializer.class)
    @JsonDeserialize(using = EnumDeserializer.class)
    private Enum<?> action; // Changed to generic Enum
    private String data;
    private String senderId;

    public EventPayload(UUID uuid, Enum<?> action, String data, String senderId) {
        this.uuid = uuid;
        this.action = action;
        this.data = data != null ? data : ""; // Ensure data is not null
        this.senderId = senderId;
    }

    public EventPayload(Enum<?> action, String data, String senderId) {
        this.action = action;
        this.data = data != null ? data : ""; // Ensure data is not null
        this.senderId = senderId;
    }

    public EventPayload(){}

    public UUID getUuid() {
        return uuid;
    }

    public Enum<?> getAction() {
        return action;
    }

    public String getData() {
        return data;
    }

    public String getSenderId() {
        return senderId;
    }

    public String toPayloadString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static EventPayload fromPayloadString(String payload) {
        try {
            return OBJECT_MAPPER.readValue(payload, EventPayload.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}