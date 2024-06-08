package com.inferris.server;

import com.inferris.commands.CommandResync;
import com.inferris.Inferris;
import com.inferris.commands.*;
import com.inferris.events.EventJoin;
import com.inferris.events.EventPing;
import com.inferris.events.EventQuit;
import com.inferris.events.EventReceive;
import com.inferris.events.redis.*;
import com.inferris.events.redis.dispatching.DispatchingJedisPubSub;
import com.inferris.events.redis.dispatching.JedisEventDispatcher;
import com.inferris.server.jedis.JedisChannel;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class Initializer {

    private static DispatchingJedisPubSub sub;

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
        pluginManager.registerCommand(instance, new CommandReport("report"));
        pluginManager.registerCommand(instance, new CommandLocate("locate"));
        pluginManager.registerCommand(instance, new CommandFriend("friend"));
        pluginManager.registerCommand(instance, new CommandResync("resync", "inferris.admin.resync"));
        pluginManager.registerCommand(instance, new CommandShout("shout"));
        pluginManager.registerCommand(instance, new CommandBuy("buy"));
        pluginManager.registerCommand(instance, new CommandPermissions("permissions", "inferris.admin.permissions"));
        pluginManager.registerCommand(instance, new CommandTrollkick("trollkick"));
        pluginManager.registerCommand(instance, new CommandDiscord("discord"));
        pluginManager.registerCommand(instance, new CommandWhoIsVanished("whoisvanished"));
        pluginManager.registerCommand(instance, new CommandAnnouncement("announce"));
        pluginManager.registerCommand(instance, new CommandStaffchatShortcut("sc"));
        pluginManager.registerCommand(instance, new CommandAdminchatShortcut("ac"));
        pluginManager.registerCommand(instance, new CommandWebsite("website"));
        pluginManager.registerCommand(instance, new CommandNuke("nuke"));
        pluginManager.registerCommand(instance, new CommandRemoveFromRedis("removefromredis"));
        pluginManager.registerCommand(instance, new CommandBungeeDev("bungeedev"));
        pluginManager.registerCommand(instance, new CommandFlagPlayer("flagplayer"));
        pluginManager.registerCommand(instance, new CommandConvertTimezone("converttime"));

        CommandViewlogs commandViewlogs = new CommandViewlogs("viewlogs");
        pluginManager.registerCommand(instance, commandViewlogs);

        plugin.getProxy().registerChannel(BungeeChannel.STAFFCHAT.getName());
        plugin.getProxy().registerChannel(BungeeChannel.PLAYER_REGISTRY.getName());
        plugin.getProxy().registerChannel(BungeeChannel.TEST.getName());
        plugin.getProxy().registerChannel(BungeeChannel.BUYCRAFT.getName());

        // Custom Redis event RECEIVE dispatch methods

        JedisEventDispatcher dispatcher = new JedisEventDispatcher();
        dispatcher.registerHandler(JedisChannel.VIEW_LOGS_SPIGOT_TO_PROXY.getChannelName(), new EventViewlog(commandViewlogs));
        dispatcher.registerHandler(JedisChannel.STAFFCHAT.getChannelName(), new EventStaffchat());
        dispatcher.registerHandler(JedisChannel.PLAYERDATA_UPDATE.getChannelName(), new EventPlayerDataUpdate());
        dispatcher.registerHandler(JedisChannel.SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE.getChannelName(), new EventUpdateDataFromSpigot());
        dispatcher.registerHandler(JedisChannel.PLAYER_FLEX_EVENT.getChannelName(), new EventPlayerFlex());
        dispatcher.registerHandler(JedisChannel.GENERIC_FLEX_EVENT.getChannelName(), new EventGenericFlex());

        sub = new DispatchingJedisPubSub(dispatcher, Inferris.getInstanceId());

        Thread subscriptionThread = new Thread(() -> Inferris.getJedisPool().getResource().subscribe(sub,
                JedisChannel.PLAYERDATA_UPDATE.getChannelName(), // Subs to the frontend to backend
                JedisChannel.SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE.getChannelName(),
                JedisChannel.VIEW_LOGS_SPIGOT_TO_PROXY.getChannelName(), JedisChannel.STAFFCHAT.getChannelName(),
                JedisChannel.PLAYER_FLEX_EVENT.getChannelName(),
                JedisChannel.GENERIC_FLEX_EVENT.getChannelName()));
        subscriptionThread.start();
    }
}
