package com.inferris.commands;

import com.inferris.rank.Branch;
import com.inferris.server.Messages;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandResync extends Command {
    public CommandResync(String name) {
        super(name);
    }

    public CommandResync(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        if (sender instanceof ProxiedPlayer player) {
            if (length > 1) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /resync OR /resync <player>"));
                return;
            }
            player.sendMessage(new TextComponent(ChatColor.GREEN + "Re-synced!"));
            if (length == 0) {
                PlayerData playerData = PlayerDataManager.getInstance().getPlayerDataFromDatabase(player.getUniqueId());
                PlayerDataManager.getInstance().updateAllData(player, playerData);
                return;
            }
            if (ProxyServer.getInstance().getPlayer(args[0]) != null) {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
                PlayerData playerData = PlayerDataManager.getInstance().getPlayerDataFromDatabase(target.getUniqueId());
                PlayerDataManager.getInstance().updateAllData(target, playerData);
            } else {
                player.sendMessage(new TextComponent(ChatColor.RED + "Player not found!"));
            }
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return PlayerDataManager.getInstance().getPlayerData((ProxiedPlayer) sender).getBranchValue(Branch.STAFF) >= 3;
    }
}
