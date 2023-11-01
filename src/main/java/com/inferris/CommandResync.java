package com.inferris;

import com.inferris.database.DatabasePool;
import com.inferris.player.Channels;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Rank;
import com.inferris.util.DatabaseUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CommandResync extends Command {
    public CommandResync(String name) {
        super(name);
    }

    public CommandResync(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        if (sender instanceof ProxiedPlayer player) {
            if (length > 1) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /resync OR /resync <player>"));
                return;
            }
            player.sendMessage(new TextComponent(ChatColor.GREEN + "Re-synced!"));
            if (length == 0) {
                PlayerData playerData = PlayerDataManager.getInstance().getPlayerDataFromDatabase(player);
                PlayerDataManager.getInstance().updateAllDataAndPush(player, playerData);
                return;
            }
            if (ProxyServer.getInstance().getPlayer(args[0]) != null) {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
                PlayerData playerData = PlayerDataManager.getInstance().getPlayerDataFromDatabase(target);
                PlayerDataManager.getInstance().updateAllDataAndPush(target, playerData);
            } else {
                player.sendMessage(new TextComponent(ChatColor.RED + "Player not found!"));
            }
        }
    }
    @Override
    protected void setPermissionMessage(String permissionMessage) {
        super.setPermissionMessage(Messages.NO_PERMISSION.getMessage().toString());
    }
}
