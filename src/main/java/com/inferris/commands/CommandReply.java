package com.inferris.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inferris.commands.cache.CommandJokeCache;
import com.inferris.commands.cache.CommandMessageCache;
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
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;

        if(sender instanceof ProxiedPlayer player){
            if(length == 0){
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /reply <message>"));
                return;
            }
            if(length >=1){

                /* If the receiver from main sender, you, is in the cache
                * As a reminder, you are KEY, receiver (original sender) is VALUE
                * The replier, you, gets the reply attachment */

                CommandMessageCache cache = CommandMessage.getCacheReplyHandler().getIfPresent(player.getUniqueId());
                if(cache != null) {

                    /* Gets the UUID of the original sender
                    * who put you in the cache */

                    UUID uuid = cache.getCache().asMap().get(player.getUniqueId());
                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(uuid);
                    String message = String.join(" ", Arrays.copyOfRange(args, 0, length));
                    if (!(RegistryManager.getInstance().getRegistry(target).getVanishState() == VanishState.ENABLED)) {

                        CommandMessage.sendMessage(player, target, message);
                        CommandMessage.getCacheReplyHandler().invalidate(target.getUniqueId());
                        CommandMessageCache targetCache = CommandMessage.getCacheReplyHandler().asMap().computeIfAbsent(target.getUniqueId(), targetUUID -> new CommandMessageCache(target, player, 5L, TimeUnit.SECONDS));
                        targetCache.add();

                    } else {
                        player.sendMessage(new TextComponent(ChatColor.RED + "Error: couldn't find that player!"));
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
                if(!(RegistryManager.getInstance().getRegistry(proxiedPlayers).getVanishState() == VanishState.ENABLED)) {
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
