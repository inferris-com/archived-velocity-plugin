package com.inferris.commands;

import com.inferris.Inferris;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Branch;
import com.inferris.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandShout extends Command {
    public CommandShout(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        StringBuilder message = new StringBuilder();
        if(length > 0) {
            for (String word : args) {
                message.append(word).append(" "); // Add a space between words
            }

            Inferris.getInstance().getLogger().info("Shout: " + message);

            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                player.sendMessage(ChatUtil.translateToHex(message.toString()));
            }
        }else{
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage error: You must provide text for the command"));
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer) {
            return PlayerDataManager.getInstance().getPlayerData((ProxiedPlayer) sender).getBranchValue(Branch.STAFF) >= 3;
        }
        return true;
    }
}