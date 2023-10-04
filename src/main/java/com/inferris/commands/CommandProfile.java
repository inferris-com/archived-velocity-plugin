package com.inferris.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.Profile;
import com.inferris.player.vanish.VanishState;
import com.inferris.server.JedisChannels;
import com.inferris.util.CacheSerializationUtils;
import com.inferris.util.DatabaseUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CommandProfile extends Command implements TabExecutor {
    public CommandProfile(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);

            if (length == 0) {
                sendPlayerProfile(player, player.getUniqueId());
            }
            if (length == 1) {
                if (args[0].equalsIgnoreCase("set")) {
                    player.sendMessage(new TextComponent(ChatColor.RESET + "Options"));
                    player.sendMessage(new TextComponent(ChatColor.YELLOW + "/profile set pronouns <pronouns>"));
                    player.sendMessage(new TextComponent(ChatColor.YELLOW + "/profile set bio <bio>"));
                    return;
                } else if (args[0].equalsIgnoreCase("unset")) {
                    player.sendMessage(new TextComponent(ChatColor.RESET + "Options"));
                    player.sendMessage(new TextComponent(ChatColor.YELLOW + "/profile unset pronouns"));
                    player.sendMessage(new TextComponent(ChatColor.YELLOW + "/profile unset bio"));
                    return;
                }
                UUID uuid = PlayerDataManager.getInstance().getUUIDByUsername(args[0]);
                if (uuid == null) {
                    player.sendMessage(new TextComponent(ChatColor.RED + "That player is not in our system."));
                    return;
                }
                sendPlayerProfile(player, uuid);
            }
            if (length == 2) {
                if (args[0].equalsIgnoreCase("unset")) {
                    if (args[1].equalsIgnoreCase("bio") || args[1].equalsIgnoreCase("pronouns")) {
                        String fieldToUnset = args[1].toLowerCase();
                        String sql = "UPDATE profile SET " + fieldToUnset + " = ? WHERE uuid = ?";
                        Object[] parameters = {null, player.getUniqueId().toString()};

                        try {
                            DatabaseUtils.executeUpdate(sql, parameters);
                            player.sendMessage(new TextComponent(ChatColor.GREEN + "Your " + fieldToUnset + " has been cleared."));
                            if (fieldToUnset.equals("bio")) {
                                playerData.getProfile().setBio(null);
                            } else if (fieldToUnset.equals("pronouns")) {
                                playerData.getProfile().setPronouns(null);
                            }

                            PlayerDataManager.getInstance().updateAllData(player, playerData);
                            try (Jedis jedis = Inferris.getJedisPool().getResource()) {
                                jedis.publish(JedisChannels.PROXY_TO_SPIGOT_PLAYERDATA_CACHE_UPDATE.getChannelName(), CacheSerializationUtils.serializePlayerData(playerData));
                            }
                        } catch (SQLException | JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            if (length >= 3) {
                if (args[0].equalsIgnoreCase("set")) {
                    if (args[1].equalsIgnoreCase("bio")) {
                        String bio = String.join(" ", Arrays.copyOfRange(args, 2, length));
                        String sql = "UPDATE profile SET bio = ? WHERE uuid = ?";
                        Object[] parameters = {bio, player.getUniqueId().toString()};
                        bio = ChatColor.translateAlternateColorCodes('&', bio.replace("\\n", "\n"));

                        try {
                            DatabaseUtils.executeUpdate(sql, parameters);
                            player.sendMessage(new TextComponent(ChatColor.GREEN + "Your bio has been successfully updated"));
                            playerData.getProfile().setBio(bio);
                            PlayerDataManager.getInstance().updateAllData(player, playerData);
                            try (Jedis jedis = Inferris.getJedisPool().getResource()) {
                                jedis.publish(JedisChannels.PROXY_TO_SPIGOT_PLAYERDATA_CACHE_UPDATE.getChannelName(), CacheSerializationUtils.serializePlayerData(playerData));
                            }
                        } catch (SQLException | JsonProcessingException e) {
                            player.sendMessage(new TextComponent(ChatColor.RED + "Uh oh, something went wrong while you were setting your bio."));
                            player.sendMessage(new TextComponent(ChatColor.RED + "Here's the stacktrace: " + ChatColor.RESET + ChatColor.ITALIC + e.getMessage()));
                            e.printStackTrace();
                        }
                    } else if (args[1].equalsIgnoreCase("pronouns")) {
                        String pronouns = String.join(" ", Arrays.copyOfRange(args, 2, length));
                        pronouns = pronouns.replace(" ", "");
                        boolean isValid = pronouns.matches("[a-zA-Z/]+");


                        if (!pronouns.contains("/")) {
                            player.sendMessage(new TextComponent(ChatColor.RED + "You must include a forward slash. Example: they/them"));
                            return;
                        }
                        if (pronouns.length() > 10) {
                            player.sendMessage(new TextComponent(ChatColor.RED + "Pronouns should be within 10 characters."));
                            return;
                        }
                        String sql = "UPDATE profile SET pronouns = ? WHERE uuid = ?";
                        Object[] parameters = {pronouns, player.getUniqueId().toString()};
                        if (isValid) {

                            try {
                                DatabaseUtils.executeUpdate(sql, parameters);
                                player.sendMessage(new TextComponent(ChatColor.GREEN + "Your pronouns have been successfully updated"));
                                playerData.getProfile().setPronouns(pronouns);
                                PlayerDataManager.getInstance().updateAllData(player, playerData);
                                try (Jedis jedis = Inferris.getJedisPool().getResource()) {
                                    jedis.publish(JedisChannels.PROXY_TO_SPIGOT_PLAYERDATA_CACHE_UPDATE.getChannelName(), CacheSerializationUtils.serializePlayerData(playerData));
                                }
                            } catch (SQLException | JsonProcessingException e) {
                                player.sendMessage(new TextComponent(ChatColor.RED + "Uh oh, something went wrong while you were setting your pronouns."));
                                player.sendMessage(new TextComponent(ChatColor.RED + "Here's the stacktrace: " + ChatColor.RESET + ChatColor.ITALIC + e.getMessage()));
                                e.printStackTrace();
                            }
                        } else {
                            player.sendMessage(new TextComponent(ChatColor.RED + "Invalid pronouns format. Please only use letters (a-z, A-Z) and slash (/), and keep it within 10 characters."));
                        }
                    }
                }
            }
        }

    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String partialPlayerName = args[0];
            List<String> completions = new ArrayList<>();

            // Add "set" option to the completions list
            if ("set".startsWith(partialPlayerName.toLowerCase())) {
                completions.add("set");
            }
            if ("unset".startsWith(partialPlayerName.toLowerCase())) {
                completions.add("unset");
            }

            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                if (!(PlayerDataManager.getInstance().getPlayerData(proxiedPlayers).getVanishState() == VanishState.ENABLED)) {
                    String playerName = proxiedPlayers.getName();
                    if (playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())) {
                        completions.add(playerName);
                    }
                }
            }
            return completions;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("unset")) {
                String partialOption = args[1].toLowerCase();
                List<String> options = new ArrayList<>();

                List<String> availableOptions = Arrays.asList("pronouns", "bio");

                for (String option : availableOptions) {
                    if (option.toLowerCase().startsWith(partialOption)) {
                        options.add(option);
                    }
                }
                return options;
            }
        }
        return Collections.emptyList();
    }


    private void sendPlayerProfile(ProxiedPlayer player, UUID targetUUID) {
        PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(targetUUID);
        Profile profile = playerData.getProfile();
        ChatColor reset = ChatColor.RESET;

        player.sendMessage(new TextComponent(ChatColor.YELLOW + "Profile of " + playerData.getNameColor() + playerData.getUsername()));
        player.sendMessage(new TextComponent(""));
        player.sendMessage(TextComponent.fromLegacyText(ChatColor.GRAY + "Rank: " + playerData.getByBranch().getPrefix()));
        if (profile.getPronouns() != null)
            player.sendMessage(new TextComponent(ChatColor.GRAY + "Pronouns: " + reset + profile.getPronouns()));
        player.sendMessage(new TextComponent(ChatColor.GRAY + "Registration date: " + reset + getFormattedRegistrationDate(profile.getRegistrationDate())));
        if (profile.getBio() != null)
            player.sendMessage(new TextComponent(ChatColor.GRAY + "Bio: " + reset + profile.getBio()));
        player.sendMessage(new TextComponent(""));

    }

    private String getFormattedRegistrationDate(LocalDate registrationDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy"); // Define the desired format pattern
        return registrationDate.format(formatter); // Format the registration date using the formatter
    }
}
