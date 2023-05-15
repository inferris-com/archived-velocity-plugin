package com.inferris.util;

import com.inferris.Inferris;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.util.UUID;
import java.util.logging.Level;

public class PlayerUtils {

    public static boolean hasJoinedBefore(String player) {
        return getUuidFromUsername(player) != null;
    }

    // Checks if the player is in the config file by checking their UUID
    public static boolean isPlayerInConfig(ProxiedPlayer player) {
        UUID playerUUID = player.getUniqueId();
        Configuration playersSection = Inferris.getPlayersConfiguration().getSection("players");

        if (playersSection == null) {
            return false; // no players in the file yet
        }

        for (String uuid : playersSection.getKeys()) {
            try {
                UUID configUUID = UUID.fromString(uuid);
                if (playerUUID.equals(configUUID)) {
                    Inferris.getInstance().getLogger().log(Level.SEVERE, "Player in config");
                    return true; // player UUID is found in the config file
                }
            } catch (IllegalArgumentException e) {
                // Handle invalid UUID string
                Inferris.getInstance().getLogger().log(Level.SEVERE, "Invalid UUID: " + uuid);
            }
        }

        Inferris.getInstance().getLogger().log(Level.SEVERE, "Player not in config");
        return false; // no matching player found
    }
    // Finds a player in the config file that matches the player's username'
    public static UUID getUuidFromUsername(String username) {
        Configuration config = Inferris.getPlayersConfiguration();
        Configuration playersSection = Inferris.getPlayersConfiguration().getSection("players");

        if (playersSection == null) {
            return null; // no players in the file yet
        }
        for (String uuid : playersSection.getKeys()) {
            String name = config.getString(uuid + ".username");

            if (name != null && name.equalsIgnoreCase(username)) {
                Inferris.getInstance().getLogger().log(Level.SEVERE, "Getting uuid from username string");
                return UUID.fromString(uuid); // found a matching player entry, return its UUID
            }
        }
        return null; // no matching player found
    }


    // Finds a player in the config file that matches the player's UUID
    public static boolean isPlayerMatchedToConfig(ProxiedPlayer player) {
        UUID playerUUID = player.getUniqueId();
        String playerName = player.getName();
        Configuration playersSection = Inferris.getPlayersConfiguration().getSection("players");

        if (playersSection == null) {
            return false; // no players in the file yet
        }

        for (String uuid : playersSection.getKeys()) {
            if (playerUUID.equals(UUID.fromString(uuid))) {
                Inferris.getInstance().getLogger().log(Level.SEVERE, "UUID match! Continuing...");
                String name = playersSection.getString(uuid + ".username");
                if (name != null && name.equalsIgnoreCase(playerName)) {
                    Inferris.getInstance().getLogger().log(Level.SEVERE, "Keys and values line up! Matched.");
                    return true; // player UUID and name match a player entry in the config file
                }
            }
        }
        return false; // no matching player found
    }
}
