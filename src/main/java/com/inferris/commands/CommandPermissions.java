package com.inferris.commands;

import com.inferris.rank.Permissions;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;


public class CommandPermissions extends Command {
    public CommandPermissions(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if (sender instanceof ProxiedPlayer player) {
            Permissions.listPermissions(player);
        }
    }
}
