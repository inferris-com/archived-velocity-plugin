package com.inferris.events;


import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class OnReceive implements Listener {

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        String tag = event.getTag();

        if (tag.equals("inferris:staffchat")) {
            DataInputStream in = new DataInputStream((new ByteArrayInputStream(event.getData())));
            try {
                String message = in.readUTF();
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
                ProxyServer.getInstance().getLogger().warning("WOOOOOOOOOOO!");
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
