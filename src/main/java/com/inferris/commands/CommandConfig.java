package com.inferris.commands;

import com.google.inject.Inject;
import com.inferris.Inferris;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.util.ConfigUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;

public class CommandConfig extends Command {
    private final PlayerDataService playerDataService;

    @Inject
    public CommandConfig(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return playerDataService.getPlayerData(player.getUniqueId()).getRank().getBranchValue(Branch.STAFF) >= 3;
        }
        return sender.getName().equalsIgnoreCase("CONSOLE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        int length = args.length;

        if (length == 2) {
            if (args[0].equalsIgnoreCase("reload")) {
                ConfigUtils.Types type = ConfigUtils.Types.valueOf(args[1].toUpperCase());
                try {

                    if (type == ConfigUtils.Types.CONFIG) {
                        //Inferris.getInstance().createPlayersConfig();
                        player.sendMessage(new TextComponent(ChatColor.GREEN + "Config reloaded! " + Inferris.getInstance()));

                        ConfigUtils.reloadConfiguration(ConfigUtils.Types.CONFIG);
                    } else if (type == ConfigUtils.Types.PROPERTIES) {
                        ConfigUtils.reloadConfiguration(ConfigUtils.Types.PROPERTIES);
                        player.sendMessage(new TextComponent(ChatColor.GREEN + "Config reloaded! " + Inferris.getInstance()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
