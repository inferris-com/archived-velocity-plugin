package com.inferris.server;

import com.inferris.CommandResync;
import com.inferris.Inferris;
import com.inferris.commands.*;
import com.inferris.database.DatabasePool;
import com.inferris.events.EventJoin;
import com.inferris.events.EventPing;
import com.inferris.events.EventQuit;
import com.inferris.events.EventReceive;
import com.inferris.player.Channels;
import com.inferris.player.vanish.VanishState;
import com.inferris.util.ConfigUtils;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class Initializer {

    // TODO: Marked for complete redo

    public static void initialize(Plugin plugin){
        Inferris instance = Inferris.getInstance();
        PluginManager pluginManager = Inferris.getInstance().getProxy().getPluginManager();;

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

        plugin.getProxy().registerChannel(BungeeChannel.STAFFCHAT.getName());
        plugin.getProxy().registerChannel(BungeeChannel.PLAYER_REGISTRY.getName());
        plugin.getProxy().registerChannel(BungeeChannel.REPORT.getName());
        plugin.getProxy().registerChannel(BungeeChannel.TEST.getName());
        plugin.getProxy().registerChannel(BungeeChannel.BUYCRAFT.getName());
    }

    @Deprecated
    public void loadPlayerRegistry() {
        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement query = connection.prepareStatement("SELECT * FROM player_data")) {

            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                String username = resultSet.getString("username");
                int vanished = resultSet.getInt("vanished");
                VanishState vanishState = VanishState.DISABLED;

                if (vanished == 1) {
                    vanishState = VanishState.ENABLED;
                }

                Configuration playersConfiguration = Inferris.getPlayersConfiguration();
                Channels channel;

                /* For broken configuration, it will fix itself */

                if (playersConfiguration.get("players." + uuid + ".channel") == null) {
                    playersConfiguration.set("players." + uuid + ".channel", "NONE");
                    //playersConfiguration.set("players." + uuid + ".vanish", "DISABLED");
                } else {

                    /* Load channel from config */

                    channel = Channels.valueOf(playersConfiguration.getString("players." + uuid + ".channel"));
                    //vanishState = VanishState.valueOf(playersConfiguration.getString("players." + uuid + ".vanish"));
                    //RegistryManager.getPlayerRegistryCache().put(UUID.fromString(uuid), new Registry(UUID.fromString(uuid), username, channel, vanishState));

                    JedisPool pool = new JedisPool("localhost", Ports.JEDIS.getPort());

                }
                ConfigUtils configUtils = new ConfigUtils();
                ConfigUtils.saveConfiguration(Inferris.getPlayersFile(), playersConfiguration);
                ConfigUtils.reloadConfiguration(ConfigUtils.Types.PLAYERS);
            }

            Inferris.getInstance().getLogger().warning("Player registry loaded successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
