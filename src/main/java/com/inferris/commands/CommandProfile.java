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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
                sendPlayerProfile(player, player);
            }
            if (length == 1) {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
                sendPlayerProfile(player, target);
            }
            if (length >= 2) {
                if (args[0].equalsIgnoreCase("set")) {
                    String bio = String.join(" ", Arrays.copyOfRange(args, 1, length));
                    String sql = "UPDATE profile SET bio = ? WHERE uuid = ?";
                    Object[] parameters = {bio, player.getUniqueId().toString()};
                    bio = ChatColor.translateAlternateColorCodes('&', bio.replace("\\n", "\n"));

                    try {
                        DatabaseUtils.executeUpdate(sql, parameters);
                        player.sendMessage(new TextComponent(ChatColor.GREEN + "Your bio has been successfully updated"));
                        playerData.getProfile().setBio(bio);
                        PlayerDataManager.getInstance().updateAllData(player, playerData);
                        try (Jedis jedis = Inferris.getJedisPool().getResource()) {
                            jedis.publish(JedisChannels.PLAYERDATA_CACHE_UPDATE.name(), CacheSerializationUtils.serializePlayerData(playerData));
                        }
                    } catch (SQLException | JsonProcessingException e) {
                        player.sendMessage(new TextComponent(ChatColor.RED + "Uh oh, something went wrong while you were setting your bio."));
                        player.sendMessage(new TextComponent(ChatColor.RED + "Here's the stacktrace: " + ChatColor.RESET + ChatColor.ITALIC + e.getMessage()));
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            String partialPlayerName = args[0];
            List<String> playerNames = new ArrayList<>();
            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                if (!(PlayerDataManager.getInstance().getPlayerData(proxiedPlayers).getVanishState() == VanishState.ENABLED)) {
                    String playerName = proxiedPlayers.getName();
                    if (playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())) {
                        playerNames.add(playerName);
                    }
                }
            }
            return playerNames;
        }
        return Collections.emptyList();
    }

    private void sendPlayerProfile(ProxiedPlayer player, ProxiedPlayer target) {
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(target);
        Profile profile = playerData.getProfile();
        ChatColor reset = ChatColor.RESET;

        player.sendMessage(new TextComponent(ChatColor.YELLOW + "Profile of " + playerData.getNameColor() + target.getName()));
        player.sendMessage(new TextComponent(""));
        player.sendMessage(new TextComponent(ChatColor.GRAY + "Rank: " + playerData.getByBranch().getPrefix()));
        player.sendMessage(new TextComponent(ChatColor.GRAY + "Pronouns: " + reset + profile.getPronouns()));
        player.sendMessage(new TextComponent(ChatColor.GRAY + "Registration date: " + reset + getFormattedRegistrationDate(profile.getRegistrationDate())));
        player.sendMessage(new TextComponent(""));
        player.sendMessage(new TextComponent(ChatColor.GRAY + "Bio: " + reset + profile.getBio()));

    }

    private String getFormattedRegistrationDate(LocalDate registrationDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy"); // Define the desired format pattern
        return registrationDate.format(formatter); // Format the registration date using the formatter
    }
}
