package com.inferris.commands;

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

            if (!(args.length == 2) && playerData.getBranchValue(Branch.STAFF) >= 3) {
                player.sendMessage(new TextComponent("Usage: /coins set <player> <amount>"));
                return;
            }

            if (args.length == 2 && playerData.getBranchValue(Branch.STAFF) >= 3) {

                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage(new TextComponent("Player " + args[0] + " not found or is offline."));
                    return;
                }
                Coins coins;
                PlayerData targetData = PlayerDataManager.getInstance().getRedisDataOrNull(target);
                if (targetData == null) {
                    player.sendMessage(new TextComponent(ChatColor.RED + "Player does not exist in our system."));
                    return;
                }

               // targetData.setCoins(args[1]);
                player.sendMessage(new TextComponent("Coins set for " + args[0] + " to " + args[1]));
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] strings) {
        return null;
    }
}
