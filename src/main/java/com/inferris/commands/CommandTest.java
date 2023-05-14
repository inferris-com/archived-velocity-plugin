package com.inferris.commands;

import com.inferris.Inferris;
import com.inferris.rank.Branch;
import com.inferris.rank.Permission;
import com.inferris.rank.RanksManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

import java.util.List;

public class CommandTest extends Command {

    public CommandTest(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer player) {


            RanksManager rankManager = RanksManager.getInstance();
            List<String> adminPermissions = Inferris.getPermissionsConfiguration().getStringList("ranks." + args[0]);


            if (args.length == 1) {
                player.sendMessage(new TextComponent(adminPermissions.toString()));
            }
        }

    }
}
