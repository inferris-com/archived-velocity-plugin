package com.inferris.commands;

import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;
import com.inferris.util.ServerUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandServerState extends Command implements TabExecutor {

    public CommandServerState(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;

        if (length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Warning! This is a " + ChatColor.ITALIC + "dangerous " + ChatColor.RED + " command, and is not suitable for production."));
            sender.sendMessage(new TextComponent(ChatColor.RED + "It should only be used when given explicit permission."));
            sender.sendMessage(new TextComponent(""));
            sender.sendMessage(new TextComponent(ChatColor.RESET + "Usage: /serverstate <mode>"));
        }
        if (length == 1) {
            if (args[0].equalsIgnoreCase("debug")) {
                ServerStateManager.setCurrentState(ServerState.DEBUG);
            }else if(args[0].equalsIgnoreCase("dev")){
                ServerStateManager.setCurrentState(ServerState.DEV);
            }else if(args[0].equalsIgnoreCase("normal") || args[0].equalsIgnoreCase("reset")){
                ServerStateManager.setCurrentState(ServerState.NORMAL);
            }
            ServerState serverState = ServerStateManager.getCurrentState();
            ChatColor reset = ChatColor.RESET;
            ChatColor yellow = ChatColor.YELLOW;
            ChatColor red = ChatColor.RED;
            ChatColor green = ChatColor.GREEN;

            ServerUtil.broadcastMessage(yellow + "=========================");
            ServerUtil.broadcastMessage(reset + "Attention players!");
            ServerUtil.broadcastMessage("");
            if(serverState != ServerState.NORMAL) {
                ServerUtil.broadcastMessage(yellow + "The network has been put into " + serverState.getColor() + serverState + yellow + " mode");
                ServerUtil.broadcastMessage("");
                ServerUtil.broadcastMessage("> " + reset + red + "This mode is not suitable for production; thus, the network may not work as intended.");
            }else{
                ServerUtil.broadcastMessage(yellow + "The network has been put into " + serverState.getColor() + serverState + yellow + " mode");
                ServerUtil.broadcastMessage("> " + reset + green + "Network main mode restored. If you encounter any issues, please rejoin. Thank you!");
            }
            ServerUtil.broadcastMessage("");
            ServerUtil.broadcastMessage(yellow + "=========================");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            String partialOption = args[0].toLowerCase();
            List<String> options = new ArrayList<>();

            List<String> availableOptions = Arrays.asList("normal", "debug", "dev");

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
