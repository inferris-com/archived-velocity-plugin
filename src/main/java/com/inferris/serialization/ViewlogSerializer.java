package com.inferris.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inferris.messaging.ViewlogMessage;
public class ViewlogSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    // Method to serialize the object to JSON
    public static String serialize(ViewlogMessage message) {
        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to deserialize the JSON string to the object
    public static ViewlogMessage deserialize(String jsonString) {
        try {
            return mapper.readValue(jsonString, ViewlogMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
