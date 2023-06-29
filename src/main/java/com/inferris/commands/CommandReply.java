package com.inferris.commands;

import com.inferris.commands.cache.CommandMessageCache;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.registry.RegistryManager;
import com.inferris.player.vanish.VanishState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandReply extends Command implements TabExecutor {
    public CommandReply(String name) {
        super(name, null, "r");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;

        if (sender instanceof ProxiedPlayer player) {
            if (length == 0) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /reply <message>"));
                return;
            }
            /* If the receiver from main sender, you, is in the cache
             * As a reminder, you are KEY, receiver (original sender) is VALUE
             * The replier, you, gets the reply attachment */

            UUID senderUUID = player.getUniqueId();
            CommandMessageCache cache = CommandMessage.getCacheReplyHandler().getIfPresent(senderUUID);

            if (cache == null) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Your reply cache has expired. Use /message instead."));
                return;
            }

            UUID targetUUID = cache.getCache().asMap().get(senderUUID);
            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetUUID);

            if (target == null) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Error: Couldn't find the original sender!"));
                return;
            }

            // Check if the target player is vanished
            if (PlayerDataManager.getInstance().getPlayerData(target).getRegistry().getVanishState() == VanishState.ENABLED) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Error: The original sender is currently vanished!"));
                return;
            }

            String message = String.join(" ", Arrays.copyOfRange(args, 0, length));
            CommandMessage.sendMessage(player, target, message);
            CommandMessage.getCacheReplyHandler().invalidate(targetUUID);
            CommandMessageCache targetCache = CommandMessage.getCacheReplyHandler().asMap().computeIfAbsent(targetUUID,
                    uuid -> new CommandMessageCache(target, player, 5L, TimeUnit.SECONDS));
            targetCache.add();
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {

        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            String partialPlayerName = args[0];
            List<String> playerNames = new ArrayList<>();
            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                if (!(PlayerDataManager.getInstance().getPlayerData(proxiedPlayers).getRegistry().getVanishState() == VanishState.ENABLED)) {
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
}