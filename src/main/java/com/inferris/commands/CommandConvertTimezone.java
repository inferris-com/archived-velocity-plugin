package com.inferris.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        TIMEZONES.put("AST", "America/Halifax");
        TIMEZONES.put("NST", "America/St_Johns");

        // South American Time Zones
        TIMEZONES.put("BRT", "America/Sao_Paulo");
        TIMEZONES.put("ART", "America/Argentina/Buenos_Aires");

        // European Time Zones
        TIMEZONES.put("GMT", "Europe/London");
        TIMEZONES.put("BST", "Europe/London"); // British Summer Time
        TIMEZONES.put("CET", "Europe/Paris");
        TIMEZONES.put("EET", "Europe/Bucharest");

        // African Time Zones
        TIMEZONES.put("CAT", "Africa/Harare");
        TIMEZONES.put("EAT", "Africa/Nairobi");

        // Asian Time Zones
        TIMEZONES.put("IST", "Asia/Kolkata");
        TIMEZONES.put("PKT", "Asia/Karachi");
        TIMEZONES.put("ICT", "Asia/Bangkok");
        TIMEZONES.put("CST", "Asia/Shanghai"); // China Standard Time
        TIMEZONES.put("JST", "Asia/Tokyo");
        TIMEZONES.put("KST", "Asia/Seoul");

        // Australian Time Zones
        TIMEZONES.put("AEST", "Australia/Sydney");
        TIMEZONES.put("ACST", "Australia/Adelaide");
        TIMEZONES.put("AWST", "Australia/Perth");

        // Pacific Time Zones
        TIMEZONES.put("NZST", "Pacific/Auckland");
        TIMEZONES.put("HST", "Pacific/Honolulu");

        // Add more timezones as needed
    }
    public CommandConvertTimezone(String name) {
        super(name);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return super.hasPermission(sender);
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter OUTPUT_FORMATTER_12 = DateTimeFormatter.ofPattern("hh:mm a z");
    private static final DateTimeFormatter OUTPUT_FORMATTER_24 = DateTimeFormatter.ofPattern("HH:mm z");

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3 || args.length > 4) {
            TextComponent textComponent = new TextComponent(ChatColor.RED + "Usage: /convert <type> <value> <timezone>\n\n");
            textComponent.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&cType: '&ftime&c' for UTC time (HH:mm) or '&ftimestamp&c' for Unix timestamp.\n\n")));
            textComponent.addExtra(ChatColor.RED + "Timezone: Use one of " + ChatColor.GRAY + TIMEZONES.keySet());
            sender.sendMessage(textComponent);
            return;
        }

        String type = args[0];
        String value = args[1];
        String targetTimezone = args[2].toUpperCase();
        boolean use24HourFormat = args.length == 4 && args[3].equalsIgnoreCase("-24");

        if (!TIMEZONES.containsKey(targetTimezone)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Invalid timezone. Use one of " + TIMEZONES.keySet().toString()));
            return;
        }

        try {
            ZonedDateTime utcZonedTime;
            if (type.equalsIgnoreCase("time")) {
                // Parse only the time part
                LocalTime utcTime = LocalTime.parse(value, TIME_FORMATTER);
                // Use the current date for conversion purposes but only display the time
                LocalDate currentDate = LocalDate.now(ZoneId.of("UTC"));
                utcZonedTime = ZonedDateTime.of(currentDate, utcTime, ZoneId.of("UTC"));
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
            String formattedTime = convertedTime.format(use24HourFormat ? OUTPUT_FORMATTER_24 : OUTPUT_FORMATTER_12);

            sender.sendMessage(new TextComponent(ChatColor.GREEN + "Converted time (" + targetTimezone + "): " + ChatColor.YELLOW + formattedTime));

        } catch (DateTimeParseException e) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Invalid time format. Ensure the format is correct (HH:mm)."));
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Invalid timestamp format. Ensure the format is correct (Unix timestamp)."));
        } catch (Exception e) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred. Please check your inputs."));
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