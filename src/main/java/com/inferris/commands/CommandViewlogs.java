package com.inferris.commands;

import com.inferris.server.*;
import com.inferris.util.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandViewlogs extends Command {
    public CommandViewlogs(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if (length == 1) {
                if (args[0].equalsIgnoreCase(Servers.LOBBY.toString().toLowerCase())) {
                    BungeeUtils.sendBungeeMessage(player, BungeeChannel.REPORT, Subchannel.REQUEST, "lobby");
                }
            }
        }
    }
}
