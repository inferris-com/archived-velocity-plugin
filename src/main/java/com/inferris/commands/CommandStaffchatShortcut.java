package com.inferris.commands;

import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Branch;
import com.inferris.rank.RankRegistry;
import com.inferris.server.Tags;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandStaffchatShortcut extends Command {
    public CommandStaffchatShortcut(String name) {
        super(name, null, "s");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer player){
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
            if(playerData.getBranchValue(Branch.STAFF) > 0){
                StringBuilder message = new StringBuilder();
                RankRegistry rank = playerData.getByBranch();

                for (String word : args) {
                    message.append(word).append(" "); // Add a space between words
                }

                BaseComponent[] textComponent = TextComponent.fromLegacyText(Tags.STAFF.getName(true)
                        + rank.getPrefix(true) + playerData.getNameColor() + player.getName() + ChatColor.RESET + ": " + message);

                for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                    if (PlayerDataManager.getInstance().getPlayerData(proxiedPlayers).getBranchValue(Branch.STAFF) >= 1) {
                        proxiedPlayers.sendMessage(textComponent);
                    }
                }
            }
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return PlayerDataManager.getInstance().getPlayerData((ProxiedPlayer) sender).isStaff();
    }
}
