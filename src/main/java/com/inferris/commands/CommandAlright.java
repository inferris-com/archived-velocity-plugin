package com.inferris.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

public class CommandAlright extends Command implements TabExecutor {
    private static Cache<UUID, CommandMessageCache> cacheReplyHandler;

    public CommandAlright(String name) {
        super(name);
        if (cacheReplyHandler == null) {
            cacheReplyHandler = Caffeine.newBuilder().build();
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "This command can only be executed by a player."));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /debug <receiver> <message>");
            return;
        }

        String receiverName = args[0];
        ProxiedPlayer receiver = ProxyServer.getInstance().getPlayer(receiverName);

        if (RegistryManager.getInstance().getRegistry(receiver).getVanishState() == VanishState.ENABLED) {
            player.sendMessage(new TextComponent(ChatColor.RED + "Error: The player is currently vanished!"));
            return;
        }

        if (receiver == null) {
            player.sendMessage(ChatColor.RED + "Error: Couldn't find that player!");
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        sendMessage(player, receiver, message);
    }
    private void sendMessage(ProxiedPlayer sender, ProxiedPlayer receiver, String message) {
        sender.sendMessage(ChatColor.GREEN + "Message sent!");
        sender.sendMessage(ChatColor.GRAY + "To " + receiver.getName() + ": " + message);
        receiver.sendMessage(ChatColor.GRAY + "From " + sender.getName() + ": " + message);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if(args.length == 1){
            List<String> list = new ArrayList<String>();
            list.add("debug");
            return list;
        }
        return Collections.emptyList();
    }
}
