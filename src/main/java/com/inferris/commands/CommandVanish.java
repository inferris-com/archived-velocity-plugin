package com.inferris.commands;

import com.inferris.Inferris;
import com.inferris.player.registry.RegistryManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.server.BungeeChannel;
import com.inferris.server.Subchannel;
import com.inferris.util.BungeeUtils;
import com.inferris.util.DatabaseUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandVanish extends Command implements TabExecutor {
    public CommandVanish(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        if(sender instanceof ProxiedPlayer player){
            if(length == 0){
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /vanish <on:off>"));
            }
            if(length == 1){
                if(args[0].equalsIgnoreCase("on")){
                    BungeeUtils.sendBungeeMessage(player, BungeeChannel.PLAYER_REGISTRY, Subchannel.VANISH, Subchannel.FORWARD, VanishState.ENABLED.name());
                    //RegistryManager.getInstance().getRegistry(player).setVanishState(VanishState.ENABLED); todo
                    updateData(player, 1);
                }
                if(args[0].equalsIgnoreCase("off")){
                    BungeeUtils.sendBungeeMessage(player, BungeeChannel.PLAYER_REGISTRY, Subchannel.VANISH, Subchannel.FORWARD, VanishState.DISABLED.name());
                    //RegistryManager.getInstance().getRegistry(player).setVanishState(VanishState.DISABLED); todo
                    updateData(player, 0);
                }
            }
        }
    }

    private void updateData(ProxiedPlayer player, int isVanished){
        String sql = "UPDATE players SET vanished = ? WHERE uuid = ?";
        Object vanished = isVanished;
        Object uuid = player.getUniqueId().toString();

        try{
            int affectedRows = DatabaseUtils.executeUpdate(sql, vanished, uuid);
            Inferris.getInstance().getLogger().info("Affected rows: " + affectedRows);
        }catch(SQLException e){
            e.printStackTrace();
        }
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer player && args.length == 1){
            List<String> list = new ArrayList<>();
            list.add("on");
            list.add("off");
            return list;
        }
        return Collections.emptyList();
    }
}
