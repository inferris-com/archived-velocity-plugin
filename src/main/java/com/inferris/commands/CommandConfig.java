package com.inferris.commands;

import com.inferris.Inferris;
import com.inferris.util.ConfigUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandConfig extends Command {
    public CommandConfig(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        int length = args.length;

        if(length == 2) {
            if (args[0].equalsIgnoreCase("reload")) {
                ConfigUtils.Types type = ConfigUtils.Types.valueOf(args[1].toUpperCase());

                if (type == ConfigUtils.Types.CONFIG) {
                    //Inferris.getInstance().createPlayersConfig();
                    player.sendMessage(new TextComponent(ChatColor.GREEN + "Config reloaded! " + Inferris.getInstance()));

                    ConfigUtils.reloadConfiguration(ConfigUtils.Types.PLAYERS);
                } else if (type == ConfigUtils.Types.PROPERTIES) {
                    ConfigUtils.reloadConfiguration(ConfigUtils.Types.PROPERTIES);
                    player.sendMessage(new TextComponent(ChatColor.GREEN + "Config reloaded! " + Inferris.getInstance()));


                }
            } else if (args[0].equalsIgnoreCase("save")) {
                ConfigUtils.Types type = ConfigUtils.Types.valueOf(args[1].toUpperCase());

                if (type == ConfigUtils.Types.PLAYERS) {
                    ConfigUtils.saveConfiguration(Inferris.getPlayersFile(), Inferris.getPlayersConfiguration());
                }
            }
        }
    }
}
