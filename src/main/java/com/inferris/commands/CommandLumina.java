package com.inferris.commands;

import com.inferris.common.ColorType;
import com.inferris.player.*;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.context.PlayerContextFactory;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;

public class CommandLumina extends Command implements TabExecutor {
    private final PlayerDataService playerDataService;

    public CommandLumina(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            PlayerDataService playerDataService = ServiceLocator.getPlayerDataService();
            PlayerContext playerContext = PlayerContextFactory.create(player.getUniqueId(), playerDataService);
            int length = args.length;
            if (length == 0) {
                int lumina = playerContext.getCoins();
                player.sendMessage(TextComponent.fromLegacyText(ChatColor.GRAY + "Lumina: " + ChatColor.of(ColorType.LUMINA.getColor()) + lumina));
                return;
            }

            if (length == 3 && playerContext.getRank().getBranchValue(Branch.STAFF) >= 3) {
                if (args[0].equalsIgnoreCase("set")) {

                    ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[1]);
                    UUID uuid;
                    if (target != null) {
                        uuid = target.getUniqueId();
                    } else {
                        uuid = playerDataService.fetchUUIDByUsername(args[1]);
                    }

                    if (uuid == null) {
                        player.sendMessage(new TextComponent(ChatColor.RED + "Player does not exist in our system."));
                        return;
                    }

                    PlayerContext targetContext = PlayerContextFactory.create(uuid, playerDataService);
                    targetContext.setCoins(Integer.parseInt(args[2]));
                    player.sendMessage(TextComponent.fromLegacyText("Lumina set for " + targetContext.getUsername() + " to " + ChatColor.of(ColorType.LUMINA.getColor()) + args[2]));
                }
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            if (playerDataService.getPlayerData(player.getUniqueId()).getRank().getBranchValue(Branch.STAFF) < 3) {
                return Collections.emptyList();
            }
        }
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
        return Collections.emptyList();
    }
}
