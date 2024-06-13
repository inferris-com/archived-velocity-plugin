package com.inferris.commands;

import com.inferris.player.*;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.context.PlayerContextFactory;
import com.inferris.player.PlayerData;
import com.inferris.player.service.PlayerDataManager;
import com.inferris.player.service.PlayerDataService;
import com.inferris.server.Message;
import com.inferris.rank.Branch;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;

public class CommandSetrank extends Command implements TabExecutor {
    private final UUID ownerUuid = UUID.fromString("7d16b15d-bb22-4a6d-80db-6213b3d75007");

    private final PlayerDataService playerDataService;
    public CommandSetrank(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        ProxiedPlayer player = null;
        if (commandSender instanceof ProxiedPlayer) {
            player = (ProxiedPlayer) commandSender;
        }

        if (player != null) {
            if (!(PlayerDataManager.getInstance().getPlayerData(player).getRank().getBranchValue(Branch.STAFF) >= 3) && !player.getUniqueId().equals(ownerUuid)) {
                player.sendMessage(Message.NO_PERMISSION.getMessage());
                return;
            }
        }

        if (args.length != 3) {
            commandSender.sendMessage(new TextComponent("Usage: /setrank <player> <branch> <ID>"));
            return;
        }

        Branch branch;
        try {
            branch = Branch.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            commandSender.sendMessage(new TextComponent("Invalid rank branch specified."));
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            commandSender.sendMessage(new TextComponent("Invalid ID specified."));
            return;
        }

        String targetName = args[0];

        UUID uuid = playerDataService.fetchUUIDByUsername(targetName);
        if (uuid == null) {
            commandSender.sendMessage(Message.PLAYER_NOT_IN_SYSTEM.getMessage());
            return;
        }

        PlayerDataService dataService = ServiceLocator.getPlayerDataService();
        PlayerContext playerContext = PlayerContextFactory.create(uuid, dataService);

        playerContext.setRank(branch, id, true);
        commandSender.sendMessage(new TextComponent("Rank set for " + args[0] + " to " + branch.name() + "-" + id));
        if (ProxyServer.getInstance().getPlayer(uuid) != null) {
            if (ProxyServer.getInstance().getPlayer(uuid).isConnected()) {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(uuid);
                assert player != null;
                PlayerContext updatedContext = PlayerContextFactory.create(uuid, dataService);

                target.sendMessage(new TextComponent(ChatColor.GREEN + "Your rank has been set to " + updatedContext.getNameColor() + updatedContext.getRank().getByBranch()));
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {

        if (sender instanceof ProxiedPlayer player) {

            if (args.length == 1) {
                String partialPlayerName = args[0];
                List<String> playerNames = new ArrayList<>();
                for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                    String playerName = proxiedPlayers.getName();
                    if (playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())) {
                        playerNames.add(playerName);
                    }
                }
                return playerNames;

            } else if (args.length == 2) {
                String partialOption = args[1].toLowerCase();
                List<String> options = new ArrayList<>();

                List<String> availableOptions = Arrays.asList("staff", "builder", "donor", "other");

                for (String option : availableOptions) {
                    if (option.toLowerCase().startsWith(partialOption)) {
                        options.add(option);
                    }
                }
                return options;

            } else if (args.length == 3) {
                String partialOption = args[1].toLowerCase();
                String partialID = args[2];
                List<String> options = new ArrayList<>();
                if (partialID.isEmpty()) {
                    switch (partialOption) {
                        case "staff", "builder" -> {
                            for (int i = 0; i <= 3; i++) {
                                options.add(String.valueOf(i));
                            }
                        }
                        case "donor" -> {
                            for (int i = 0; i <= 1; i++) {
                                options.add(String.valueOf(i));
                            }
                        }
                        case "other" -> {
                            for (int i = 0; i <= 0; i++) {
                                options.add(String.valueOf(i));
                            }
                        }
                    }
                } else if (isNumeric(partialID)) {
                    int selectedID = Integer.parseInt(partialID);
                    if (partialOption.equals("staff") && selectedID >= 0 && selectedID <= 4) {
                        options.add(String.valueOf(selectedID));
                    } else if (partialOption.equals("donor") && selectedID >= 0 && selectedID <= 1) {
                        options.add(String.valueOf(selectedID));
                    } else if (partialOption.equals("other") && selectedID >= 0 && selectedID <= 3) {
                        options.add(String.valueOf(selectedID));
                    }
                }
                return options;
            }
        }
        return Collections.emptyList();
    }

    private boolean isNumeric(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}