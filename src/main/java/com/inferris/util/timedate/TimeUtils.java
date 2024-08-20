package com.inferris.util.timedate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class TimeUtils {
    public static String getCurrentTimeUTC() {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("mm:ss z")
                .toFormatter()
                .withZone(ZoneId.of("UTC"));

        return formatter.format(Instant.now());
    }
}
