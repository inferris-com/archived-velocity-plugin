package com.inferris.commands;

import com.inferris.Inferris;
import com.inferris.config.ConfigType;
import com.inferris.database.DatabasePool;
import com.inferris.player.service.PlayerDataManager;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.Profile;
import com.inferris.util.ContentTypes;
import com.inferris.util.DatabaseUtils;
import com.inferris.util.RestClientManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import okhttp3.MediaType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandUnlink extends Command implements TabExecutor {
    private static final String API_BASE_URL = "https://inferris.com/api/";
    String API_KEY = Inferris.getInstance().getConfigurationHandler().getProperties(ConfigType.PROPERTIES).getProperty("xf.api.key");

    private final PlayerDataService playerDataService;

    public CommandUnlink(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (length != 1) {
            player.sendMessage(ChatColor.GOLD + "===============");
            player.sendMessage(ChatColor.GREEN + "Unlink your account");
            player.sendMessage("");
            player.sendMessage(ChatColor.YELLOW + "If you want to unlink your forum account, confirm by using " +
                    ChatColor.RED + "/unlink confirm");
            player.sendMessage(ChatColor.GOLD + "===============");
        }
        if (length == 1 && args[0].equalsIgnoreCase("confirm")) {
            Profile profile = PlayerDataManager.getInstance().getPlayerData(player).getProfile();
            boolean isVerified = false;
            try (Connection connection = DatabasePool.getConnection()) {
                String condition = "uuid = '" + player.getUniqueId() + "'";
                ResultSet rs = DatabaseUtils.queryData(connection, "verification", new String[]{"*"}, condition);
                if (rs.next()) {
                    isVerified = true;
                }

                if (isVerified) {
                    condition = "uuid = ?";
                    PreparedStatement statement = connection.prepareStatement("DELETE FROM verification WHERE " + condition);
                    statement.setObject(1, player.getUniqueId().toString());
                    statement.executeUpdate();
                    player.sendMessage(ChatColor.GREEN + "Successfully unlinked your forum account.");

                    try (RestClientManager clientManager = new RestClientManager()) {
                        clientManager.sendRequest(API_BASE_URL + "users/" + profile.getXenforoId() + "/?=&custom_fields[minecraft]=",
                                RestClientManager.Method.POST, API_KEY, MediaType.parse(ContentTypes.PLAIN.getType()), "");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Your forum account is not linked.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + "An unexpected error has occurred. Details: " + ChatColor.RESET + e.getMessage());
            }
        }
        return;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("confirm");
            return list;
        }
        return Collections.emptyList();
    }
}
