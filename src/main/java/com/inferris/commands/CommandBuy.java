package com.inferris.commands;

import com.inferris.server.BungeeChannel;
import com.inferris.server.Subchannel;
import com.inferris.util.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandBuy extends Command {
    public CommandBuy(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if(sender instanceof ProxiedPlayer player){
            BungeeUtils.sendBungeeMessage(player, BungeeChannel.BUYCRAFT, Subchannel.FORWARD);
        }
    }
}
