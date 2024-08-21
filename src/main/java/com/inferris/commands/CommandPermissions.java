package com.inferris.commands;

import com.google.inject.Inject;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Permissions;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandPermissions extends Command {

    private final PlayerDataService playerDataService;
    private final Permissions permissions;

    @Inject
    public CommandPermissions(String name, PlayerDataService playerDataService, Permissions permissions) {
        super(name);
        this.playerDataService = playerDataService;
        this.permissions = permissions;
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        if (sender instanceof ProxiedPlayer player) {
            permissions.listPermissions(player);
        }
    }
}
