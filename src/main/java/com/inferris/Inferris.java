package com.inferris;

import com.inferris.commands.CommandViewlogs;
import com.inferris.config.ConfigType;
import com.inferris.config.ConfigurationHandler;
import com.inferris.database.DatabasePool;
import com.inferris.events.redis.*;
import com.inferris.events.redis.dispatching.DispatchingJedisPubSub;
import com.inferris.events.redis.EventPlayerFlex;
import com.inferris.events.redis.dispatching.JedisEventDispatcher;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.server.*;
import com.inferris.server.jedis.JedisChannels;
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
        dispatcher.registerHandler(JedisChannels.PLAYERDATA_UPDATE.getChannelName(), new EventPlayerDataUpdate());
        dispatcher.registerHandler(JedisChannels.SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE.getChannelName(), new EventUpdateDataFromSpigot());
        dispatcher.registerHandler(JedisChannels.PLAYER_FLEX_EVENT.getChannelName(), new EventPlayerFlex());
        dispatcher.registerHandler(JedisChannels.GENERIC_FLEX_EVENT.getChannelName(), new EventGenericFlex());

        String instanceId = "backend"; // Unique identifier for this instance
        DispatchingJedisPubSub jedisPubSub = new DispatchingJedisPubSub(dispatcher, instanceId);

        jedisPool = new JedisPool(configurationHandler.getProperties(ConfigType.PROPERTIES).getProperty("address"), Port.JEDIS.getPort());
        Thread subscriptionThread = new Thread(() -> Inferris.getJedisPool().getResource().subscribe(jedisPubSub,
                JedisChannels.PLAYERDATA_UPDATE.getChannelName(), // Subs to the frontend to backend
                JedisChannels.SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE.getChannelName(),
                JedisChannels.VIEW_LOGS_SPIGOT_TO_PROXY.getChannelName(), JedisChannels.STAFFCHAT.getChannelName(),
                JedisChannels.PLAYER_FLEX_EVENT.getChannelName(),
                JedisChannels.GENERIC_FLEX_EVENT.getChannelName()));
        subscriptionThread.start();

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
