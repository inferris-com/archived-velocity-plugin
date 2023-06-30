package com.inferris.commands;

import com.inferris.Messages;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandSetrank extends Command implements TabExecutor {
    public CommandSetrank(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) commandSender;
        if(!(PlayerDataManager.getInstance().getPlayerData(player).getBranchValue(Branch.STAFF) >=3)){
            player.sendMessage(Messages.NO_PERMISSION.getMessage());
            return;
        }
        if (args.length != 3) {
            player.sendMessage(new TextComponent("Usage: /setrank <player> <branch> <ID>"));
            return;
        }

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
        if(target == null){
            player.sendMessage(new TextComponent("Player " + args[0] + " not found or is offline."));
            return;
        }

        Branch branch;
        try{
            branch = Branch.valueOf(args[1].toUpperCase());
        }catch(IllegalArgumentException e){
            player.sendMessage(new TextComponent("Invalid rank branch specified."));
            return;
        }

        int id;
        try {
            id = Integer.parseInt(args[2]);
        }catch(NumberFormatException e){
            player.sendMessage(new TextComponent("Invalid ID specified."));
            return;
        }

        PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(target);
        if(playerData == null){
            player.sendMessage(new TextComponent(ChatColor.RED + "Player does not exist in our system."));
            return;
        }
        playerData.setRank(branch, id);
        player.sendMessage(new TextComponent("Rank set for " + args[0] + " to " + branch.name() + "-" + id));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {

        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            String partialPlayerName = args[0];
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
