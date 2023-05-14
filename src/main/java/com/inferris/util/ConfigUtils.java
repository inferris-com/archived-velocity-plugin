package com.inferris.util;

import java.util.Map;

public class ConfigUtils {

    public static Object getNestedValue(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object value = data.get(key);
            if (value == null) {
                return null;
            } else if (value instanceof Map) {
                data = (Map<String, Object>) value;
            } else {
                return value;
            }
        }
        return null;
    }

}
