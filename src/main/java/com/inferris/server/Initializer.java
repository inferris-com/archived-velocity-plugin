package com.inferris.server;

import com.inferris.commands.CommandResync;
import com.inferris.Inferris;
import com.inferris.commands.*;
import com.inferris.events.EventJoin;
import com.inferris.events.EventPing;
import com.inferris.events.EventQuit;
import com.inferris.events.EventReceive;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class Initializer {

    // TODO: Marked for complete redo

    public static void initialize(Plugin plugin){
        Inferris instance = Inferris.getInstance();
        PluginManager pluginManager = Inferris.getInstance().getProxy().getPluginManager();


        pluginManager.registerListener(instance, new EventJoin());
        pluginManager.registerListener(instance, new EventQuit());
        pluginManager.registerListener(instance, new EventReceive());
        pluginManager.registerListener(instance, new EventPing());

        pluginManager.registerCommand(instance, new CommandBungeeTest("bungeetest"));
        pluginManager.registerCommand(instance, new CommandConfig("config"));
        pluginManager.registerCommand(instance, new CommandMessage("message"));
        pluginManager.registerCommand(instance, new CommandReply("reply"));
        pluginManager.registerCommand(instance, new CommandChannel("channel"));
        pluginManager.registerCommand(instance, new CommandVanish("vanish"));
        pluginManager.registerCommand(instance, new CommandVerify("verify"));
        pluginManager.registerCommand(instance, new CommandUnlink("unlink"));
        pluginManager.registerCommand(instance, new CommandSetrank("rank"));
        pluginManager.registerCommand(instance, new CommandCoins("coins"));
        pluginManager.registerCommand(instance, new CommandProfile("profile"));
        pluginManager.registerCommand(instance, new CommandAccount("account"));
        pluginManager.registerCommand(instance, new CommandServerState("serverstate"));
        pluginManager.registerCommand(instance, new CommandViewlogs("viewlogs"));
        pluginManager.registerCommand(instance, new CommandReport("report"));
        pluginManager.registerCommand(instance, new CommandLocate("locate"));
        pluginManager.registerCommand(instance, new CommandFriend("friend"));
        pluginManager.registerCommand(instance, new CommandResync("resync", "inferris.admin.resync"));
        pluginManager.registerCommand(instance, new CommandShout("shout"));
        pluginManager.registerCommand(instance, new CommandBuy("buy"));
        pluginManager.registerCommand(instance, new CommandPermissions("permissions", "inferris.admin.permissions"));
        pluginManager.registerCommand(instance, new CommandTrollkick("trollkick"));
        pluginManager.registerCommand(instance, new CommandDiscord("discord"));

        plugin.getProxy().registerChannel(BungeeChannel.STAFFCHAT.getName());
        plugin.getProxy().registerChannel(BungeeChannel.PLAYER_REGISTRY.getName());
        plugin.getProxy().registerChannel(BungeeChannel.REPORT.getName());
        plugin.getProxy().registerChannel(BungeeChannel.TEST.getName());
        plugin.getProxy().registerChannel(BungeeChannel.BUYCRAFT.getName());
    }
}
