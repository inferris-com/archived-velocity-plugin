package com.inferris.commands;

import com.google.inject.Inject;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.player.PlayerData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandResync extends Command {
    private final PlayerDataService playerDataService;

    @Inject
    public CommandResync(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
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
                return;
            }
            if (ProxyServer.getInstance().getPlayer(args[0]) != null) {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
                PlayerData playerData = playerDataService.fetchPlayerDataFromDatabase(target.getUniqueId());
                playerDataService.updatePlayerData(player.getUniqueId(), playerData1 -> playerDataService.fetchPlayerDataFromDatabase(player.getUniqueId()));
            } else {
                player.sendMessage(new TextComponent(ChatColor.RED + "Player not found!"));
            }
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return playerDataService.getPlayerData(player.getUniqueId()).getRank().getBranchValue(Branch.STAFF) >= 3;
        }
        return false;
    }
}
