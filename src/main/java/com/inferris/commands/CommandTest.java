package com.inferris.commands;

import com.inferris.*;
import com.inferris.player.Channels;
import com.inferris.player.registry.Registry;
import com.inferris.player.registry.RegistryManager;
import com.inferris.player.vanish.VanishState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;

import java.util.List;
import java.util.UUID;

public class CommandTest extends Command {

    public CommandTest(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if(length == 1){
                if(args[0].equalsIgnoreCase("registry")) {
                    for (UUID uuid : RegistryManager.getPlayerRegistryCache().asMap().keySet()) {
                        player.sendMessage(new TextComponent(uuid.toString()));
                    }
                }
                if(args[0].equalsIgnoreCase("players")){
                    Configuration configuration = Inferris.getPlayersConfiguration().getSection("players");
                    player.sendMessage(new TextComponent(configuration.get(player.getUniqueId() + ".channel").toString()));

                }
            }
            
            if (length == 2) {
                if (args[0].equalsIgnoreCase("perms")) {
                    List<String> adminPermissions = Inferris.getPermissionsConfiguration().getStringList("ranks." + args[1]);
                    player.sendMessage(new TextComponent(adminPermissions.toString()));

                }else if(args[0].equalsIgnoreCase("registry")){
                    if(args[1].equalsIgnoreCase("invalidate")){
                        RegistryManager.getPlayerRegistryCache().asMap().clear();
                        player.sendMessage(new TextComponent(ChatColor.GREEN + "Invalidated registry"));

                    }else if(args[1].equalsIgnoreCase("reload")){
                        RegistryManager.getPlayerRegistryCache().asMap().clear();
                        new Initializer().loadPlayerRegistry();
                        player.sendMessage(new TextComponent(ChatColor.GREEN + "Reloaded registry"));
                    }
                }
            }

            if (length == 3) {
                if (args[0].equalsIgnoreCase("registry") && args[1].equalsIgnoreCase("remove")) {
                    UUID uuid = UUID.fromString(args[2]);
                    RegistryManager.getPlayerRegistryCache().invalidate(UUID.fromString(args[2]));
                    player.sendMessage(new TextComponent(ChatColor.GREEN + "Removed " + uuid + " from registry"));
                }
            }


            if(length == 4){
                if(args[0].equalsIgnoreCase("registry")) {
                    //debug 1.registry 2.add 3.uuid 4.username
                    if(args[1].equalsIgnoreCase("add")) {
                        UUID uuid = UUID.fromString(args[2]);
                        String username = args[3];
                        if(player.getUniqueId().equals(uuid)){
                            RegistryManager.getPlayerRegistryCache().invalidate(player.getUniqueId());
                        }
                        RegistryManager.getPlayerRegistryCache().put(uuid, new Registry(uuid, username, Channels.NONE, VanishState.DISABLED));
                        player.sendMessage(new TextComponent(ChatColor.GREEN + "Added " + username + " to registry"));
                    }
                }
            }
        }
    }
}
