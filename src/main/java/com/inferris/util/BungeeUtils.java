package com.inferris.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.inferris.server.BungeeChannel;
import com.inferris.server.Subchannel;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeUtils {
    private static final String CHANNEL_NAME = "BungeeCord";

    public static void sendBungeeMessage(ProxiedPlayer player, BungeeChannel channel, Subchannel subchannel, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subchannel.toLowerCase());
        out.writeUTF(message);
        player.getServer().sendData(channel.getName(), out.toByteArray());
    }

    public static void sendBungeeMessage(ProxiedPlayer player, BungeeChannel channel, String subchannel, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subchannel.toLowerCase());
        out.writeUTF(message);
        player.getServer().sendData(channel.getName(), out.toByteArray());
    }

    public static void sendBungeeMessage(ProxiedPlayer player, BungeeChannel channel, Subchannel subchannel) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subchannel.toLowerCase());
        player.getServer().sendData(channel.getName(), out.toByteArray());
    }

    public static void sendBungeeMessage(ProxiedPlayer player, BungeeChannel channel, Subchannel subchannel, Subchannel subchannel2, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subchannel.toLowerCase());
        out.writeUTF(subchannel2.toLowerCase());
        out.writeUTF(message);
        player.getServer().sendData(channel.getName(), out.toByteArray());
    }


    public static void sendBungeeMessage(ProxiedPlayer player, BungeeChannel channel, String subchannel, String subchannel2, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subchannel.toLowerCase());
        out.writeUTF(subchannel2.toLowerCase());
        out.writeUTF(message);
        player.getServer().sendData(channel.getName(), out.toByteArray());
    }
}