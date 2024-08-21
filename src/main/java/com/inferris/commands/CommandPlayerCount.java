package com.inferris.commands;

import com.google.inject.Inject;
import com.inferris.Inferris;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.server.PlayerCountManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandPlayerCount extends Command {
    private final PlayerDataService playerDataService;
    private int totalCount;

    @Inject
    public CommandPlayerCount(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return playerDataService.getPlayerData(player.getUniqueId()).getRank().getBranchValue(Branch.STAFF) >= 3;
        }
        return false;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if (args.length != 1 && args.length != 2) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /playercount <increment:decrement:reset> [value]"));
                return;
            }

            String action = args[0];
            if (action.equalsIgnoreCase("increment") && args.length == 2) {
                handleIncrement(player, args[1]);
            } else if (action.equalsIgnoreCase("decrement") && args.length == 2) {
                handleDecrement(player, args[1]);
            } else if (action.equalsIgnoreCase("reset") && args.length == 1) {
                handleReset(player);
            } else {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /playercount <increment:decrement:reset> [value]"));
            }
        }
    }

    private void handleIncrement(ProxiedPlayer player, String valueStr) {
        try {
            int value = Integer.parseInt(valueStr);
            int currentCount = PlayerCountManager.getOverriddenCount() != null ? PlayerCountManager.getOverriddenCount() : Inferris.getInstance().getVisibleOnlinePlayers();
            PlayerCountManager.setOverriddenCount(currentCount + value);
            player.sendMessage(new TextComponent(ChatColor.GREEN + "Player count incremented by " + value + "."));
        } catch (NumberFormatException e) {
            player.sendMessage(new TextComponent(ChatColor.RED + "The value must be an integer."));
        }
    }

    private void handleDecrement(ProxiedPlayer player, String valueStr) {
        try {
            int value = Integer.parseInt(valueStr);
            int currentCount = PlayerCountManager.getOverriddenCount() != null ? PlayerCountManager.getOverriddenCount() :  Inferris.getInstance().getVisibleOnlinePlayers();
            PlayerCountManager.setOverriddenCount(currentCount - value);
            player.sendMessage(new TextComponent(ChatColor.GREEN + "Player count decremented by " + value + "."));
        } catch (NumberFormatException e) {
            player.sendMessage(new TextComponent(ChatColor.RED + "The value must be an integer."));
        }
    }

    private void handleReset(ProxiedPlayer player) {
        PlayerCountManager.resetOverriddenCount();
        player.sendMessage(new TextComponent(ChatColor.GREEN + "Player count override has been reset."));
    }
}