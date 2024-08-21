package com.inferris.commands;

import com.google.inject.Inject;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.PlayerData;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.server.Message;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandLocate extends Command implements TabExecutor {
    private final PlayerDataService playerDataService;

    @Inject
    public CommandLocate(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {

        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            String partialPlayerName = args[0];
            List<String> playerNames = new ArrayList<>();
            PlayerData playerData = playerDataService.getPlayerData(player.getUniqueId());
            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                if (playerDataService.getPlayerData(proxiedPlayers.getUniqueId()).getVanishState() == VanishState.DISABLED || playerData.getRank().getBranchValue(Branch.STAFF) >= 3) {
                    String playerName = proxiedPlayers.getName();
                    if (playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())) {
                        playerNames.add(playerName);
                    }
                }
            }
            return playerNames;
        }
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        int length = args.length;

        if (length == 0 || length > 1) {
            player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /locate <player>"));
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(new TextComponent(Message.COULD_NOT_FIND_PLAYER.getMessage()));
            return;
        }

        PlayerContext playerContext = new PlayerContext(player.getUniqueId(), playerDataService);
        PlayerContext targetPlayerContext = new PlayerContext(target.getUniqueId(), playerDataService);

        if (targetPlayerContext.getVanishState() == VanishState.ENABLED) {
            if (playerContext.getRank().getBranchValue(Branch.STAFF) < 3) {
                player.sendMessage(new TextComponent(Message.COULD_NOT_FIND_PLAYER.getMessage()));
                return;
            }
        }

        player.sendMessage(TextComponent.fromLegacyText(ChatColor.GRAY + "Player " +
                targetPlayerContext.getNameColor() +
                targetPlayerContext.getRank().getByBranch().getPrefix(true) + ChatColor.RESET + targetPlayerContext.getUsername() + ChatColor.GRAY +
                " is " + ChatColor.GREEN + "online"));
        player.sendMessage(TextComponent.fromLegacyText(ChatColor.GRAY + "Server: " + ChatColor.GOLD + targetPlayerContext.getCurrentServer().converted()));
    }
}
