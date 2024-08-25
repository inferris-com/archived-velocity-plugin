/*
 * Copyright (c) 2024. Inferris.
 * All rights reserved.
 */

package com.inferris.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.inferris.Inferris;
import com.inferris.events.redis.EventPayload;
import com.inferris.events.redis.GenericAction;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.server.CustomError;
import com.inferris.server.ErrorCode;
import com.inferris.server.jedis.JedisChannel;
import com.inferris.server.jedis.JedisHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.scheduler.TaskScheduler;

import java.util.concurrent.TimeUnit;

public class CommandKillInstances extends Command {
    private final PlayerDataService playerDataService;
    private final Cache<String, Boolean> confirmationCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES) // Set TTL to 2 minutes
            .build();

    @Inject
    public CommandKillInstances(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        ProxiedPlayer player = null;
        if (sender instanceof ProxiedPlayer) {
            player = (ProxiedPlayer) sender;
        }
        if (player != null) {
            return playerDataService.getPlayerData(player.getUniqueId()).getRank().getBranchValue(Branch.STAFF) >= 3;
        } else {
            return sender.getName().equals("CONSOLE");
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        String senderName = sender.getName();

        if (length == 0) {
            // Command without flags, add to cache with false boolean
            confirmationCache.put(senderName, false);
            sender.sendMessage(new TextComponent(ChatColor.RED + "This is a dangerous command that will shut down all front-end Paper instances."));
            sender.sendMessage(new TextComponent(ChatColor.RED + "To proceed, use " + ChatColor.RESET + "/killinstances confirm"));
            return;
        }
        if (length == 1) {
            if (args[0].equalsIgnoreCase("confirm")) {
                Boolean shutdownFlag = confirmationCache.getIfPresent(senderName);
                if (shutdownFlag != null) {
                    // Shutdown with appropriate flag
                    shutdownServers(sender, shutdownFlag);
                    confirmationCache.invalidate(senderName);
                } else {
                    sender.sendMessage(new TextComponent(ChatColor.RED + "No pending shutdown confirmation found. Use the command again first."));
                }
            } else if (args[0].equalsIgnoreCase("-p")) {
                // Command with -p flag, add to cache with true boolean
                confirmationCache.put(senderName, true);
                sender.sendMessage(new TextComponent(ChatColor.RED + "This is a dangerous command that will shut down all front-end Paper instances with the -p flag."));
                sender.sendMessage(new TextComponent(ChatColor.RED + "To proceed, use " + ChatColor.RESET + "/killinstances confirm"));
            }
        }
    }

    private void shutdownServers(CommandSender sender, boolean hasFlag) {
        TaskScheduler scheduler = Inferris.getInstance().getProxy().getScheduler();
        String name = ChatColor.RED +  sender.getName();
        if(sender instanceof ProxiedPlayer){
            name = new PlayerContext(((ProxiedPlayer) sender).getUniqueId(), playerDataService).getNameColor() + name;
        }

        for (ProxiedPlayer all : ProxyServer.getInstance().getPlayers()) {
            all.sendMessage(new TextComponent("Backend: Shutting down all instances..."));
        }

        String finalName = name;
        scheduler.schedule(Inferris.getInstance(), new Runnable() {
            @Override
            public void run() {
                JedisHelper.publish(JedisChannel.GENERIC_FLEX_EVENT, new EventPayload(GenericAction.SHUTDOWN, null, Inferris.getInstanceId()));

                if(hasFlag){
                    ProxyServer.getInstance().stop(new CustomError(ErrorCode.NETWORK_KILLED)
                            .getTemplate( "Details: Used by " + finalName + ChatColor.WHITE + ". Hang tight! <3").getText());
                }
            }
        }, 5L, TimeUnit.SECONDS);
    }
}
