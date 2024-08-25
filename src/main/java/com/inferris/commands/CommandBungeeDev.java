package com.inferris.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.inferris.Inferris;
import com.inferris.events.redis.EventPayload;
import com.inferris.events.redis.PlayerAction;
import com.inferris.player.*;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.manager.PlayerDataManager;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.server.ErrorCode;
import com.inferris.server.jedis.JedisChannel;
import com.inferris.util.SerializationUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class CommandBungeeDev extends Command {
    private final PlayerDataService playerDataService;

    @Inject
    public CommandBungeeDev(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;

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
                            PlayerDataManager playerDataManager = Inferris.getInstance().getInjector().getInstance(PlayerDataManager.class);

                            String localPlayerData = SerializationUtils.serializePlayerData(playerDataService.getPlayerData(targetUUID));
                            String redisPlayerData = SerializationUtils.serializePlayerData(playerDataManager.getRedisData(targetUUID));

                            player.sendMessage("Local: " + localPlayerData);
                            player.sendMessage("Redis: " + redisPlayerData);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    case "service" -> {
                        try {
                            player.sendMessage(SerializationUtils.serializePlayerData(playerDataService.getPlayerData(player.getUniqueId())));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    case "end" -> {
                        ProxyServer.getInstance().stop(ChatColor.GRAY + "Woa! An issue has occurred: " + ErrorCode.PROXY_STOPPED_BY_ADMIN.getCode(true)
                                + "\n\n" + ErrorCode.PROXY_STOPPED_BY_ADMIN.getMessage(true) + "\n\n"
                                + ChatColor.WHITE + "Not to fret! They're probably fixin' up an issue\n or deploying a patch. Hang tight!");
                    }
                    case "cache2" -> {
                        PlayerDataManager playerDataManager = Inferris.getInstance().getInjector().getInstance(PlayerDataManager.class);
                        player.sendMessage(new TextComponent(String.valueOf(playerDataManager.getCache().getIfPresent(player.getUniqueId()).getProfile().toString())));
                    }
                }
            }
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            PlayerContext playerContext = new PlayerContext(player.getUniqueId(), playerDataService);
            Rank rank = playerContext.getRank();
            return rank.getBranchValue(Branch.STAFF) >= 3 || player.getUniqueId().toString().equals("7d16b15d-bb22-4a6d-80db-6213b3d75007");
        }
        return false;
    }
}
