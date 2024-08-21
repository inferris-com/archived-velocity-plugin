package com.inferris;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.inferris.config.ConfigType;
import com.inferris.config.ConfigurationHandler;
import com.inferris.database.DatabasePool;
import com.inferris.player.PlayerData;
import com.inferris.player.manager.PlayerDataManager;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.service.PlayerDataServiceImpl;
import com.inferris.player.vanish.VanishState;
import com.inferris.server.*;
import com.inferris.util.JedisBuilder;
import com.inferris.util.timedate.TimeUtils;
import com.inferris.webhook.WebhookBuilder;
import com.inferris.webhook.WebhookType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import redis.clients.jedis.JedisPool;

import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class Inferris extends Plugin {
    private static Inferris instance;
    private static JedisPool jedisPool;
    private Injector injector;
    private final ConfigurationHandler configurationHandler = ConfigurationHandler.getInstance();
    private PlayerDataManager playerDataManager;
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

        String redisAddress = configurationHandler.getProperties(ConfigType.PROPERTIES).getProperty("address");
        String redisPassword = configurationHandler.getProperties(ConfigType.PROPERTIES).getProperty("redis.password");
        int redisPort = Port.JEDIS.getPort();
        JedisBuilder jedisBuilder = new JedisBuilder(redisAddress, redisPort);
        jedisBuilder.setPassword(redisPassword);
        jedisPool = jedisBuilder.build();

        // Initialize Guice
        injector = Guice.createInjector(new GuiceModule());

        // Inject and initialize necessary services and managers
        this.playerDataManager = injector.getInstance(PlayerDataManager.class);
        PlayerDataService playerDataService = injector.getInstance(PlayerDataServiceImpl.class);

        // Initialize other services and managers

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

        sendStatusWebhook("Backend Notification", "Backend proxy is now online [" + TimeUtils.getCurrentTimeUTC() + "]", new Color(122, 237, 90));

        String debugMode = configurationHandler.getProperties(ConfigType.PROPERTIES).getProperty("debug.mode");
        if (debugMode != null && debugMode.equalsIgnoreCase("true")) {
            ServerStateManager.setCurrentState(ServerState.DEBUG);
            getLogger().warning("============================");
            getLogger().warning("Debug is enabled!");
            getLogger().warning("============================");
        } else {
            ServerStateManager.setCurrentState(ServerState.NORMAL);
        }

        Initializer initializer = new Initializer(playerDataService, this, injector);
        initializer.initialize();
    }

    public Injector getInjector() {
        return injector;
    }

    @Override
    public void onDisable() {
        jedisPool.getResource().close();
        sendStatusWebhook("Backend Notification", "Backend proxy is now offline [" + TimeUtils.getCurrentTimeUTC() + "]", new Color(255, 112, 112));
    }

    public int getTotalVanishedPlayers() {
        int vanishedCount = 0;
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            PlayerData playerData = playerDataManager.getPlayerData(player);
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

    private void sendStatusWebhook(String title, String description, Color color) {
        WebhookBuilder webhookBuilder = new WebhookBuilder(WebhookType.STATUS)
                .setTitle(title)
                .setDescription(description)
                .setColor(color)
                .build();
        webhookBuilder.sendEmbed();
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
