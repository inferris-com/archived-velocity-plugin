package com.inferris;

import com.inferris.commands.CommandViewlogs;
import com.inferris.config.ConfigType;
import com.inferris.config.ConfigurationHandler;
import com.inferris.database.DatabasePool;
import com.inferris.events.redis.*;
import com.inferris.events.redis.dispatching.DispatchingJedisPubSub;
import com.inferris.events.redis.dispatching.JedisEventDispatcher;
import com.inferris.server.*;
import com.inferris.server.jedis.JedisChannels;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.JedisPool;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class Inferris extends Plugin {
    private static Inferris instance;
    private static JedisPool jedisPool;
    private final ConfigurationHandler configurationHandler = ConfigurationHandler.getInstance();

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Inferris module is enabled!");
        try {
            configurationHandler.loadConfig(ConfigType.CONFIG);
            configurationHandler.loadConfig(ConfigType.PERMISSIONS);
            configurationHandler.loadProperties(ConfigType.PROPERTIES);
            configurationHandler.loadProperties(ConfigType.DATABASE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Initializer.initialize(this);

        try {
            Connection connection = DatabasePool.getConnection();
            if (connection.isClosed()) {
                getLogger().log(Level.SEVERE, "Database connection is closed!");
            } else {
                getLogger().log(Level.INFO, "Connected to database!");
            }
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, e.getMessage());
        }

        // Custom Redis event RECEIVE dispatch methods
        CommandViewlogs commandViewlogs = new CommandViewlogs("viewlogs");
        getProxy().getPluginManager().registerCommand(this, commandViewlogs);

        JedisEventDispatcher dispatcher = new JedisEventDispatcher();
        dispatcher.registerHandler(JedisChannels.VIEW_LOGS_SPIGOT_TO_PROXY.getChannelName(), new EventViewlog(commandViewlogs));
        dispatcher.registerHandler(JedisChannels.STAFFCHAT.getChannelName(), new EventStaffchat());
        dispatcher.registerHandler(JedisChannels.PLAYERDATA_UPDATE_TO_BACKEND.getChannelName(), new EventPlayerDataUpdate());
        dispatcher.registerHandler(JedisChannels.SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE.getChannelName(), new EventUpdateDataFromSpigot());

        DispatchingJedisPubSub jedisPubSub = new DispatchingJedisPubSub(dispatcher);

        jedisPool = new JedisPool(configurationHandler.getProperties(ConfigType.PROPERTIES).getProperty("address"), Port.JEDIS.getPort());
        Thread subscriptionThread = new Thread(() -> Inferris.getJedisPool().getResource().subscribe(jedisPubSub,
                JedisChannels.PLAYERDATA_UPDATE_TO_BACKEND.getChannelName(), // Subs to the frontend to backend
                JedisChannels.SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE.getChannelName(),
                JedisChannels.VIEW_LOGS_SPIGOT_TO_PROXY.getChannelName(), JedisChannels.STAFFCHAT.getChannelName()));
        subscriptionThread.start();

        String debugMode = configurationHandler.getProperties(ConfigType.PROPERTIES).getProperty("debug.mode");
        if(debugMode != null && debugMode.equalsIgnoreCase("true")){
            ServerStateManager.setCurrentState(ServerState.DEBUG);
            getLogger().warning("============================");
            getLogger().warning("Debug is enabled!");
            getLogger().warning("============================");

        }else{
            ServerStateManager.setCurrentState(ServerState.NORMAL);
        }
    }

    @Override
    public void onDisable() {
        jedisPool.getResource().close();
    }

    public ConfigurationHandler getConfigurationHandler() {
        return configurationHandler;
    }

    public static Inferris getInstance() {
        return instance;
    }

    public static JedisPool getJedisPool() {
        return jedisPool;
    }
}
