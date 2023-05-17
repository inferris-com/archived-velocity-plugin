package com.inferris.commands;

import com.inferris.Inferris;
import com.inferris.util.ConfigUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
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
        ConfigUtils configUtils = new ConfigUtils();

        if(length == 1){
            if(args[0].equalsIgnoreCase("reload")){
                Inferris.getInstance().createPlayersConfig();
                player.sendMessage(ChatColor.GREEN + "Config reloaded! " + Inferris.getInstance());

                configUtils.reloadConfiguration(ConfigUtils.Types.PLAYERS);
            }else if(args[0].equalsIgnoreCase("save")){
                configUtils.saveConfiguration(Inferris.getPlayersFile(), Inferris.getPlayersConfiguration());

            }
        }
    }
}
