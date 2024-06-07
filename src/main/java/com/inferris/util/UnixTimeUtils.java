package com.inferris.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UnixTimeUtils {
    // Method to get the formatted date with a custom pattern
    public static String getFormattedDate(Long unixTime, String pattern) {
        Instant instant = (unixTime == null) ? Instant.now() : Instant.ofEpochSecond(unixTime);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));
        return zonedDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    // Method to get only the date part in the default format (yyyy-MM-dd)
    public static String getDateOnly(Long unixTime) {
        return getFormattedDate(unixTime, "yyyy-MM-dd");
    }

    // Method to get only the time part in the default format (HH:mm 'UTC')
    public static String getTimeOnly(Long unixTime) {
        return getFormattedDate(unixTime, "HH:mm z");
    }

    // Method to get both date and time in the default format (yyyy-MM-dd HH:mm 'UTC')
    public static String getDateTime(Long unixTime) {
        return getFormattedDate(unixTime, "yyyy-MM-dd HH:mm z");
    }
}