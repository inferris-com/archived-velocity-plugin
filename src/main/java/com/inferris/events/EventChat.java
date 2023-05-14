package com.inferris.events;

import com.inferris.channel.ChannelManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class EventChat implements Listener {

    static boolean isCancelled = false;

    @EventHandler
    public void onChat(ChatEvent event) {
//        String message = event.getMessage();
//        ProxiedPlayer sender = (ProxiedPlayer) event.getSender();
//        Collection<ProxiedPlayer> recipients = new HashSet<>();
//
//        ChannelManager channelManager = ChannelManager.getInstance();
//        switch (channelManager.getChannel(sender)) {
//            case STAFF, SPECIAL -> {
//                if (!event.isCommand()) {
//
//                    for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
//                        if (player.hasPermission("inf.staff")) {
//                            recipients.add(player);
//                        }
//                    }
//
//                    if (sender.hasPermission("inf.staff")) {
//                        for (ProxiedPlayer player : recipients) {
//                            player.sendMessage(message);
//                        }
//                    }
//                }
//            }
//        }
//    }
    }
}



//        ChannelManager channelManager = ChannelManager.getInstance();
//        switch (channelManager.getChannel(player)) {
//            case STAFF, SPECIAL -> {
//                if (!event.isCommand()) {
//                    event.setCancelled(true);
//                    channelManager.sendMessage(player, channelManager.getChannel(player), event.getMessage());
//                }
//            }
//        }
//
//        if(isCancelled) {
//            if (!event.getMessage().equalsIgnoreCase("uncancel")) {
//                event.setCancelled(true);
//            }
//        }
//
//
//        if (event.getMessage().equalsIgnoreCase("cancel")) {
//            isCancelled = true;
//        }else if (event.getMessage().equalsIgnoreCase("uncancel")){
//            isCancelled = false;
//        }
