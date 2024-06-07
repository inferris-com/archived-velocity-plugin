package com.inferris.events.redis;

import java.util.UUID;

public class EventPayload {
    private final UUID uuid;
    private final Action action;
    private final String data;

    public EventPayload(UUID uuid, Action action, String data) {
        this.uuid = uuid;
        this.action = action;
        this.data = data;
    }
    public UUID getUuid() {
        return uuid;
    }

    public Action getAction() {
        return action;
    }

    public String getData() {
        return data;
    }

    public String toPayloadString() {
        return uuid.toString() + ":" + action.name() + ":" + data;
    }

    public static EventPayload fromPayloadString(String payload) {
        String[] parts = payload.split(":", 3);
        UUID uuid = UUID.fromString(parts[0]);
        Action action = Action.valueOf(parts[1]);
        String data = parts.length > 2 ? parts[2] : "";
        return new EventPayload(uuid, action, data);
    }

    public enum Action {
        NOTIFY, WELCOME
    }
}