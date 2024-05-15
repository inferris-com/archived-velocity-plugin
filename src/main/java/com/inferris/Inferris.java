package com.inferris;

import com.inferris.config.ConfigType;
import com.inferris.config.ConfigurationHandler;
import com.inferris.database.DatabasePool;
import com.inferris.events.*;
import com.inferris.server.*;
import com.inferris.server.jedis.JedisChannels;
import com.inferris.util.ConfigUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import redis.clients.jedis.JedisPool;

import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
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
        JedisReceive jedisReceive = new JedisReceive();

        try {
            Connection connection = DatabasePool.getConnection();
            if (connection.isClosed()) {
                getLogger().log(Level.SEVERE, "Database connection is closed!");
            } else {
                getLogger().log(Level.WARNING, "Connected to database!");
            }
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, e.getMessage());
        }

        jedisPool = new JedisPool("198.27.83.200", Ports.JEDIS.getPort());
        Thread subscriptionThread = new Thread(() -> Inferris.getJedisPool().getResource().subscribe(jedisReceive,
                JedisChannels.PLAYERDATA_UPDATE.getChannelName(),
                JedisChannels.SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE.getChannelName(),
                JedisChannels.VIEW_LOGS_SPIGOT_TO_PROXY.getChannelName(), JedisChannels.STAFFCHAT.getChannelName()));
        subscriptionThread.start();

        //StatusUpdater statusUpdater = new StatusUpdater(ProxyServer.getInstance().getScheduler());

        String debugMode = configurationHandler.getConfig(ConfigType.CONFIG).getString("debug.mode");
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
        jedisPool.close();
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
