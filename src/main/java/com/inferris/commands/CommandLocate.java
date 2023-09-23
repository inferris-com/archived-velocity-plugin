package com.inferris.commands;

import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandLocate extends Command implements TabExecutor {
    public CommandLocate(String name) {
        super(name);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        int length = args.length;

        if(length == 0 || length > 1){
            player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /locate <player>"));
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(new TextComponent(ChatColor.RED + "Error: Could not find player!"));
            return;
        }

        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
        PlayerData targetData = PlayerDataManager.getInstance().getPlayerData(target);
        if (targetData.getVanishState() == VanishState.ENABLED) {
            if(playerData.getBranchValue(Branch.STAFF) < 3) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Error: Could not find player!"));
                return;
            }
        }

        player.sendMessage(new TextComponent(ChatColor.GRAY + "Player " +
                targetData.getNameColor() +
                targetData.getByBranch().getPrefix(true) + targetData.getUsername() + ChatColor.GRAY +
                " is " + ChatColor.GREEN + "online"));
        player.sendMessage(new TextComponent(ChatColor.GRAY + "Server: " + ChatColor.GOLD + targetData.getCurrentServer().converted()));
    }
}
