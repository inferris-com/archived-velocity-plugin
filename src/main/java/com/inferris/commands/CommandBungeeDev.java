package com.inferris.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.inferris.Inferris;
import com.inferris.player.*;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.manager.ManagerContainer;
import com.inferris.player.manager.PlayerDataManager;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.server.CustomError;
import com.inferris.server.ErrorCode;
import com.inferris.util.SerializationUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CommandBungeeDev extends Command {
    private final PlayerDataService playerDataService;
    private final ManagerContainer managerContainer;

    @Inject
    public CommandBungeeDev(String name, PlayerDataService playerDataService, ManagerContainer managerContainer) {
        super(name);
        this.playerDataService = playerDataService;
        this.managerContainer = managerContainer;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            PlayerContext playerContext = new PlayerContext(player.getUniqueId(), playerDataService);
            Rank rank = playerContext.getRank();
            return rank.getBranchValue(Branch.STAFF) >= 3 || player.getUniqueId().toString().equals("7d16b15d-bb22-4a6d-80db-6213b3d75007");
        } else {
            return true;
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;

        if (sender instanceof ProxiedPlayer player) {

            if (length == 0) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Command usage is not available here due to how unstable the command can be."));
                return;
            }
            if (length == 1 || length == 2) {
                String action = args[0].toLowerCase();
                UUID targetUUID = player.getUniqueId();

                if (length == 2) {
                    ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(args[1]);
                    if (targetPlayer == null) {
                        player.sendMessage(new TextComponent(ChatColor.RED + "Player not found."));
                        return;
                    }
                    targetUUID = targetPlayer.getUniqueId();
                }

                switch (action) {
                    case "cache" -> {
                        try {

                            String localPlayerData = SerializationUtils.serializePlayerData(playerDataService.getPlayerData(targetUUID));
                            String redisPlayerData = SerializationUtils.serializePlayerData(managerContainer.getPlayerDataManager().getRedisData(targetUUID));

                            player.sendMessage("Local: " + localPlayerData);
                            player.sendMessage("Redis: " + redisPlayerData);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    case "removecaches" -> ProxyServer.getInstance().getScheduler().schedule(Inferris.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            managerContainer.getPlayerDataManager().getCache().invalidateAll();
                            for (ProxiedPlayer allPlayers : ProxyServer.getInstance().getPlayers()) {
                                allPlayers.sendMessage(new TextComponent("All locally cached player data has been removed by " + player.getName()));
                                allPlayers.sendMessage(new TextComponent("Size: " + managerContainer.getPlayerDataManager().getCache().asMap().size()));
                            }
                        }
                    }, 1, TimeUnit.SECONDS);
                    case "cachelist" -> {
                        // Access the cache map directly without triggering any loading
                        Map<UUID, PlayerData> cache = managerContainer.getPlayerDataManager().getCache().asMap();

                        // Use the cached UUIDs to get names of online players only
                        String names = cache.keySet().stream()
                                .map(uuid -> ProxyServer.getInstance().getPlayer(uuid)) // Get the ProxiedPlayer
                                .filter(Objects::nonNull)           // Ensure the player is online
                                .map(ProxiedPlayer::getName)        // Get the player's name
                                .collect(Collectors.joining(", ")); // Join names with commas

                        TextComponent message = new TextComponent(names.isEmpty() ? "No cached players online." : names);
                        sender.sendMessage(message);
                    }
                    case "service" -> {
                        try {
                            player.sendMessage(SerializationUtils.serializePlayerData(playerDataService.getPlayerData(player.getUniqueId())));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    case "end" -> {
                        ProxyServer.getInstance().stop(new CustomError(ErrorCode.PROXY_STOPPED_BY_ADMIN).getTemplate().getText());
                    }
                    case "cache2" -> {
                        PlayerDataManager playerDataManager = Inferris.getInstance().getInjector().getInstance(PlayerDataManager.class);
                        player.sendMessage(new TextComponent(String.valueOf(playerDataManager.getCache().getIfPresent(player.getUniqueId()).getProfile().toString())));
                    }
                }
            }
        } else {
            if (length == 1) {
                String action = args[0].toLowerCase();

                switch (action) {
                    case "name":
                        sender.sendMessage(sender.getName());
                }
            }
        }
    }
}
