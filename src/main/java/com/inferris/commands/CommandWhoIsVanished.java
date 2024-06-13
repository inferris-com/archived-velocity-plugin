package com.inferris.commands;

import com.inferris.player.PlayerData;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;
import java.util.List;

public class CommandWhoIsVanished extends Command {
    private final PlayerDataService playerDataService;

    public CommandWhoIsVanished(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return playerDataService.getPlayerData(player.getUniqueId()).getRank().getBranchValue(Branch.STAFF) >= 3;
        }
        return sender.getName().equalsIgnoreCase("CONSOLE");
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ChatColor.RESET).append("Vanished admins\n").append(ChatColor.RESET);

        // Collect vanished players
        List<ProxiedPlayer> vanishedPlayers = new ArrayList<>();
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            PlayerData playerData = playerDataService.getPlayerData(player.getUniqueId());
            if (playerData.getVanishState() == VanishState.ENABLED) {
                vanishedPlayers.add(player);
            }
        }

        // Append vanished players to the stringBuilder
        for (int i = 0; i < vanishedPlayers.size(); i++) {
            ProxiedPlayer player = vanishedPlayers.get(i);
            PlayerData playerData = playerDataService.getPlayerData(player.getUniqueId());
            stringBuilder.append(playerData.getRank().getByBranch().getPrefix(true)).append(ChatColor.RESET).append(player.getName());
            stringBuilder.append(ChatColor.DARK_GRAY + " - " + ChatColor.GREEN + playerData.getCurrentServer());

            // Add comma and space except for the last player
            if (i < vanishedPlayers.size() - 1) {
                stringBuilder.append(ChatColor.YELLOW).append(", ").append(ChatColor.RESET);
            }
        }
        sender.sendMessage(TextComponent.fromLegacyText(stringBuilder.toString()));
    }
}