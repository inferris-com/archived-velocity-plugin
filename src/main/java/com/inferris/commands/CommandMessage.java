package com.inferris.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Rank;
import com.inferris.rank.RankRegistry;
import com.inferris.rank.RanksManager;
import com.inferris.util.BungeeChannels;
import com.inferris.util.Subchannel;
import kotlin.collections.ArrayDeque;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandMessage extends Command implements TabExecutor {
    public CommandMessage(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        if(sender instanceof ProxiedPlayer player) {
            if (length == 0 || length == 1) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /message <player> <message>"));
            }
            if (length >= 2) {
                if (ProxyServer.getInstance().getPlayer(args[0]) != null) {
                    ProxiedPlayer receiver = ProxyServer.getInstance().getPlayer(args[0]);
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();

                    /*
                    We might not need rankdata here, because we may make the registry update from Spigot, and/or
                    put our setrank command on BungeeCord.
                     */

/*
                    out.writeUTF(BungeeChannels.DIRECT_MESSAGE.getName());
                    out.writeUTF(Subchannel.REQUEST.toLowerCase());
                    out.writeUTF("rankdata");
*/

                    out.writeUTF(BungeeChannels.DIRECT_MESSAGE.getName());
                    out.writeUTF(Subchannel.FORWARD.toLowerCase());
                    String message = String.join(" ", Arrays.copyOfRange(args, 1, length));
                    out.writeUTF(message);

                    receiver.getServer().sendData(BungeeChannels.DIRECT_MESSAGE.getName(), out.toByteArray());
                    RankRegistry playerRank = PlayerDataManager.getInstance().getPlayerData(player).getByBranch();
                    RankRegistry receiverRank = PlayerDataManager.getInstance().getPlayerData(receiver).getByBranch();
                    message = message + ChatColor.RESET;

                    player.sendMessage(new TextComponent(ChatColor.GREEN + "Message sent!"));
                    player.sendMessage(new TextComponent(ChatColor.GRAY + "To " + receiverRank.getPrefix(true) + receiver.getName() + ChatColor.RESET + ": "+ message));
                    receiver.sendMessage(new TextComponent(ChatColor.GRAY + "From " + playerRank.getPrefix(true) + player.getName() + ChatColor.RESET + ": "+ message));
                }
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {

        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            String partialPlayerName = args[0];
            List<String> playerNames = new ArrayList<>();
            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                String playerName = proxiedPlayers.getName();
                if(playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())){
                    playerNames.add(playerName);
                }
            }
            return playerNames;
        }
        return Collections.emptyList();
    }
}
