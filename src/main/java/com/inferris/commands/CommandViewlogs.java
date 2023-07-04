package com.inferris.commands;

import com.inferris.server.*;
import com.inferris.util.BungeeUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * The CommandViewlogs class represents a command used to view logs of a specific server in a game proxy.
 * It allows players to request and view the logs of a particular server.
 * The command can be executed by players to retrieve logs for analysis or debugging purposes.
 * The requested server is specified as a command argument.
 * If the server is valid, the command sends a Bungee message to retrieve the logs for the requested server.
 *
 * @since 1.0
 */

public class CommandViewlogs extends Command {
    public CommandViewlogs(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if (length == 1) {
                String requestedServer = args[0].toLowerCase();
                if(!isValidServer(requestedServer)){
                    player.sendMessage(new TextComponent(ChatColor.RED + "Error: Invalid server!"));
                    return;
                }
                BungeeUtils.sendBungeeMessage(player, BungeeChannel.REPORT, Subchannel.REQUEST, requestedServer);
            }
        }
    }

    /**
     * Checks if the specified server is valid.
     * It compares the server name with the valid servers defined in the Servers enum.
     *
     * @param server the server name to validate
     * @return true if the server is valid, false otherwise
     */

    private boolean isValidServer(String server) {
        for (Servers validServer : Servers.values()) {
            if (validServer.toString().toLowerCase().equals(server)) {
                return true;
            }
        }
        return false;
    }
}
