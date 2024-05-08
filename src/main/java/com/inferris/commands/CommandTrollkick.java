package com.inferris.commands;

import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.server.Messages;
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

public class CommandTrollkick extends Command implements TabExecutor {
    public CommandTrollkick(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = null;
        int length = args.length;

        if (sender instanceof ProxiedPlayer) {
            player = (ProxiedPlayer) sender;
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);

            if (playerData.getBranchValue(Branch.STAFF) < 2) {
                // Insufficient permissions, send NO_PERMISSION message and exit
                player.sendMessage(Messages.NO_PERMISSION.getMessage());
                return;
            }

            if (length == 1) {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
                if (playerData.getBranchValue(Branch.STAFF) >= 2) {
                    target.disconnect(new TextComponent(""));
                }
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {

        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
            if (playerData.getBranchValue(Branch.STAFF) >= 2) {
                String partialPlayerName = args[0];
                List<String> playerNames = new ArrayList<>();
                for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                    if (PlayerDataManager.getInstance().getPlayerData(proxiedPlayers).getVanishState() == VanishState.DISABLED || playerData.getBranchValue(Branch.STAFF) >= 3) {
                        String playerName = proxiedPlayers.getName();
                        if (playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())) {
                            playerNames.add(playerName);
                        }
                    }
                }
                return playerNames;
            }
        }
        return Collections.emptyList();
    }
}
