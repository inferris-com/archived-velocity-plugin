package com.inferris;

import com.inferris.config.ConfigType;
import com.inferris.config.ConfigurationHandler;
import com.inferris.database.DatabasePool;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.server.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
    private static final String INSTANCE_ID = "backend";

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

        jedisPool = new JedisPool(configurationHandler.getProperties(ConfigType.PROPERTIES).getProperty("address"), Port.JEDIS.getPort());
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

        String debugMode = configurationHandler.getProperties(ConfigType.PROPERTIES).getProperty("debug.mode");
        if (debugMode != null && debugMode.equalsIgnoreCase("true")) {
            ServerStateManager.setCurrentState(ServerState.DEBUG);
            getLogger().warning("============================");
            getLogger().warning("Debug is enabled!");
            getLogger().warning("============================");
        } else {
            ServerStateManager.setCurrentState(ServerState.NORMAL);
        }
    }

    @Override
    public void onDisable() {
        jedisPool.getResource().close();
    }

    public int getTotalVanishedPlayers() {
        int vanishedCount = 0;
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
            if (playerData.getVanishState() == VanishState.ENABLED) {
                vanishedCount++;
            }
        }
        return vanishedCount;
    }

    public int getOnlinePlayers() {
        return ProxyServer.getInstance().getOnlineCount();
    }

    public int getVisibleOnlinePlayers() {
        int count = ProxyServer.getInstance().getOnlineCount();
        if (count >= 1) {
            count = count - getTotalVanishedPlayers();
        }
        return count;
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

    public static String getInstanceId() {
        return INSTANCE_ID;
    }
}
