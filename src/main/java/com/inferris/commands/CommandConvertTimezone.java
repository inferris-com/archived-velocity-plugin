package com.inferris.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CommandConvertTimezone extends Command {
    private static final Map<String, String> TIMEZONES;

    static {
        TIMEZONES = new HashMap<>();
        TIMEZONES.put("PST", "America/Los_Angeles");
        TIMEZONES.put("MST", "America/Denver");
        TIMEZONES.put("CST", "America/Chicago");
        TIMEZONES.put("EST", "America/New_York");
        TIMEZONES.put("GMT", "Europe/London");
        TIMEZONES.put("CET", "Europe/Paris");
        TIMEZONES.put("IST", "Asia/Kolkata");
        TIMEZONES.put("JST", "Asia/Tokyo");
        TIMEZONES.put("AEST", "Australia/Sydney");
        // Add more timezones as needed
    }
    public CommandConvertTimezone(String name) {
        super(name);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return super.hasPermission(sender);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 3) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /convert <type> <value> <timezone>"));
            sender.sendMessage(new TextComponent(ChatColor.RED + "Type: 'time' for UTC time (HH:mm) or 'timestamp' for Unix timestamp."));
            sender.sendMessage(new TextComponent(ChatColor.RED + "Timezone: Use one of " + TIMEZONES.keySet().toString()));
            return;
        }

        String type = args[0];
        String value = args[1];
        String targetTimezone = args[2].toUpperCase();

        if (!TIMEZONES.containsKey(targetTimezone)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Invalid timezone. Use one of " + TIMEZONES.keySet().toString()));
            return;
        }

        try {
            ZonedDateTime utcZonedTime;
            if (type.equalsIgnoreCase("time")) {
                LocalDateTime utcTime = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"));
                utcZonedTime = utcTime.atZone(ZoneId.of("UTC"));
            } else if (type.equalsIgnoreCase("timestamp")) {
                long unixTimestamp = Long.parseLong(value);
                Instant instant = Instant.ofEpochSecond(unixTimestamp);
                utcZonedTime = instant.atZone(ZoneId.of("UTC"));
            } else {
                sender.sendMessage(new TextComponent(ChatColor.RED + "Invalid type. Use 'time' or 'timestamp'."));
                return;
            }

            ZoneId zoneId = ZoneId.of(TIMEZONES.get(targetTimezone));
            ZonedDateTime convertedTime = utcZonedTime.withZoneSameInstant(zoneId);
            String formattedTime = convertedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"));
            sender.sendMessage(new TextComponent(ChatColor.GREEN + "Converted time (" + targetTimezone + "): " + ChatColor.YELLOW + formattedTime));

        } catch (Exception e) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Invalid value. Ensure the format is correct."));
        }
    }

    private void sendConvertedTimes(CommandSender sender, ZonedDateTime utcZonedTime) {
        StringBuilder message = new StringBuilder(ChatColor.GREEN + "Converted times:\n");
        for (Map.Entry<String, String> entry : TIMEZONES.entrySet()) {
            String timezone = entry.getKey();
            ZoneId zoneId = ZoneId.of(entry.getValue());
            ZonedDateTime convertedTime = utcZonedTime.withZoneSameInstant(zoneId);
            String formattedTime = convertedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"));
            message.append(ChatColor.AQUA).append(timezone).append(": ").append(ChatColor.YELLOW).append(formattedTime).append("\n");
        }
        sender.sendMessage(new TextComponent(message.toString()));
    }
}