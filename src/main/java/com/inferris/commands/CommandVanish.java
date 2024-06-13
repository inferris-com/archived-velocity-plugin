package com.inferris.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.events.redis.EventPayload;
import com.inferris.events.redis.PlayerAction;
import com.inferris.player.PlayerData;
import com.inferris.player.service.PlayerDataManager;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.server.Message;
import com.inferris.server.jedis.JedisChannel;
import com.inferris.util.SerializationUtils;
import com.inferris.util.DatabaseUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import redis.clients.jedis.Jedis;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandVanish extends Command implements TabExecutor {
    private final PlayerDataService playerDataService;

    public CommandVanish(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        if (sender instanceof ProxiedPlayer player) {
            PlayerData playerData = playerDataService.getPlayerData(player.getUniqueId());

            if (playerData.getRank().getBranchValue(Branch.STAFF) >= 3) {
                if (length == 0 || length > 1) {
                    player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /vanish <on:off>"));
                    return;
                }

                if (args[0].equalsIgnoreCase("on")) {
                    updateDatabase(player, 1);
                    updatePlayerData(player, VanishState.ENABLED);
                    //BungeeUtils.sendBungeeMessage(player, BungeeChannel.PLAYER_DATA, Subchannel.VANISH, Subchannel.FORWARD, VanishState.ENABLED.name());
                }
                if (args[0].equalsIgnoreCase("off")) {
                    updateDatabase(player, 0);
                    updatePlayerData(player, VanishState.DISABLED);
                    //BungeeUtils.sendBungeeMessage(player, BungeeChannel.PLAYER_DATA, Subchannel.VANISH, Subchannel.FORWARD, VanishState.ENABLED.name());
                }

                if (args[0].equalsIgnoreCase("join")) {
                    try (Jedis jedis = Inferris.getJedisPool().getResource()) {
                        jedis.publish(JedisChannel.PLAYERDATA_VANISH.getChannelName(), new EventPayload(player.getUniqueId(),
                                PlayerAction.VANISH,
                                "join",
                                Inferris.getInstanceId()).toPayloadString());
                    }
                }

                if (args[0].equalsIgnoreCase("quit")) {
                    try (Jedis jedis = Inferris.getJedisPool().getResource()) {
                        jedis.publish(JedisChannel.PLAYERDATA_VANISH.getChannelName(), new EventPayload(player.getUniqueId(),
                                PlayerAction.VANISH,
                                "quit",
                                Inferris.getInstanceId()).toPayloadString());
                    }
                }
            } else {
                player.sendMessage(Message.NO_PERMISSION.getMessage());
            }
        }
    }

    private void updateDatabase(ProxiedPlayer player, int isVanished) {
        String sql = "UPDATE player_data SET vanished = ? WHERE uuid = ?";
        Object vanished = isVanished;
        Object uuid = player.getUniqueId().toString();

        try {
            int affectedRows = DatabaseUtils.executeUpdate(sql, vanished, uuid);
            Inferris.getInstance().getLogger().info("Affected rows: " + affectedRows);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePlayerData(ProxiedPlayer player, VanishState vanishState) {
        PlayerData playerData = playerDataService.getPlayerData(player.getUniqueId());
        playerData.setVanishState(vanishState);
        PlayerDataManager.getInstance().updateAllDataAndPush(player, playerData, JedisChannel.PLAYERDATA_VANISH);
        String json;
        try {
            json = SerializationUtils.serializePlayerData(playerData);
            Inferris.getInstance().getLogger().warning("Bungee json: " + json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof ProxiedPlayer) {
            String partialOption = args[0].toLowerCase();
            List<String> options = new ArrayList<>();

            List<String> availableOptions = Arrays.asList("off", "on", "join", "quit");

            for (String option : availableOptions) {
                if (option.toLowerCase().startsWith(partialOption)) {
                    options.add(option);
                }
            }
            return options;
        }
        return Collections.emptyList();
    }
}
