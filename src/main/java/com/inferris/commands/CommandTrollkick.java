package com.inferris.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandTrollkick extends Command implements TabExecutor {
    public CommandTrollkick(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = null;
        int length = args.length;

        if(sender instanceof ProxiedPlayer){
            player = (ProxiedPlayer) sender;
        }

        if(length == 0 || length > 1){
            sender.sendMessage(new TextComponent(ChatColor.RED + "That player is not online."));
            return;
        }

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
        target.disconnect(new TextComponent(""));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return null;
    }
}
