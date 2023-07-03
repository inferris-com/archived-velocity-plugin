package com.inferris.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.player.coins.Coins;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Branch;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;

public class CommandCoins extends Command implements TabExecutor {
    public CommandCoins(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
            int length = args.length;
            if (length == 0) {
                int coins = playerData.getCoins().getBalance();
                player.sendMessage(new TextComponent(ChatColor.GRAY + "Coin balance: " + ChatColor.YELLOW + coins));
                return;
            }

            if (length == 3 && playerData.getBranchValue(Branch.STAFF) >= 3) {
                if (args[0].equalsIgnoreCase("set")) {

                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                    UUID uuid = null;
                    try {
                        uuid = PlayerDataManager.getInstance().getUUIDByUsername(args[1]);
                    }catch(Exception e){
                        e.printStackTrace();
                        player.sendMessage(new TextComponent(ChatColor.RED + "Player does not exist in our system."));
                        return;
                    }

                    PlayerData targetData = PlayerDataManager.getInstance().getRedisDataOrNull(uuid);
                    if (targetData == null) {
                        player.sendMessage(new TextComponent(ChatColor.RED + "Player does not exist in our system."));
                        return;
                    }

                    targetData.setCoins(Integer.parseInt(args[2]));
                    player.sendMessage(new TextComponent("Coins set for " + targetData.getRegistry().getUsername() + " to " + ChatColor.AQUA + args[2]));
                }
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            if (args.length == 1) {
                String partialOption = args[0].toLowerCase();
                List<String> options = new ArrayList<>();

                List<String> availableOptions = List.of("set");

                for (String option : availableOptions) {
                    if (option.toLowerCase().startsWith(partialOption)) {
                        options.add(option);
                    }
                }
                return options;
            }
            if (args.length == 2) {
                String partialPlayerName = args[1];
                List<String> playerNames = new ArrayList<>();
                for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                    String playerName = proxiedPlayers.getName();
                    if (playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())) {
                        playerNames.add(playerName);
                    }
                }
                return playerNames;
            }
        }
        return Collections.emptyList();
    }
}
