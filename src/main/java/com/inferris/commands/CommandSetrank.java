package com.inferris.commands;

import com.inferris.Messages;
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

public class CommandSetrank extends Command implements TabExecutor {
    private final UUID uuid = UUID.fromString("7d16b15d-bb22-4a6d-80db-6213b3d75007");
    public CommandSetrank(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if (!(PlayerDataManager.getInstance().getPlayerData(player).getBranchValue(Branch.STAFF) >= 3) || !player.getUniqueId().equals(uuid)) {
            player.sendMessage(Messages.NO_PERMISSION.getMessage());
            return;
        }

        if (args.length != 3) {
            player.sendMessage(new TextComponent("Usage: /setrank <player> <branch> <ID>"));
            return;
        }

        Branch branch;
        try {
            branch = Branch.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(new TextComponent("Invalid rank branch specified."));
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(new TextComponent("Invalid ID specified."));
            return;
        }

        String targetName = args[0];
        PlayerDataManager playerDataManager = PlayerDataManager.getInstance();

        UUID uuid = playerDataManager.getUUIDByUsername(targetName);
        if (uuid == null) {
            player.sendMessage(Messages.PLAYER_NOT_IN_SYSTEM.getMessage());
            return;
        }

        PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(uuid);
        playerData.setRank(branch, id, true);
        player.sendMessage(new TextComponent("Rank set for " + args[0] + " to " + branch.name() + "-" + id));
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

                List<String> availableOptions = Arrays.asList("staff", "donor", "other");

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
                    if (partialOption.equals("staff")) {
                        for (int i = 0; i <= 3; i++) {
                            options.add(String.valueOf(i));
                        }
                    } else if (partialOption.equals("donor")) {
                        for (int i = 0; i <= 1; i++) {
                            options.add(String.valueOf(i));
                        }
                    } else if (partialOption.equals("other")) {
                        for (int i = 0; i <= 0; i++) {
                            options.add(String.valueOf(i));
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