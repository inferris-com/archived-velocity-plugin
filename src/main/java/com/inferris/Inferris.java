package com.inferris;

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
    private static Properties properties;
    private static File propertiesFile;
    private static File configFile;
    private static File permissionsFile;
    private static File playersFile;
    private static Configuration configuration;
    private static Configuration permissionsConfiguration;
    private static Configuration playersConfiguration;
    private static JedisPool jedisPool;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Inferris module is enabled!");
        createConfig();
        createPermissionsConfig();
        createPlayersConfig();
        createProperties();

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

        playersFile = new File(getDataFolder(), "players.yml");
        playersConfiguration = ConfigUtils.createConfigFile(playersFile, "players");

        jedisPool = new JedisPool("198.27.83.200", Ports.JEDIS.getPort());
        Thread subscriptionThread = new Thread(() -> Inferris.getJedisPool().getResource().subscribe(jedisReceive,
                JedisChannels.SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE.getChannelName(),
                JedisChannels.VIEW_LOGS_SPIGOT_TO_PROXY.getChannelName(), JedisChannels.STAFFCHAT.getChannelName()));
        subscriptionThread.start();

        //StatusUpdater statusUpdater = new StatusUpdater(ProxyServer.getInstance().getScheduler());

        String debugMode = properties.getProperty("debug.mode");
        if(debugMode != null && debugMode.equalsIgnoreCase("true")){
            ServerStateManager.setCurrentState(ServerState.DEBUG);
            getLogger().warning("============================");
            getLogger().warning("Debug is enabled!");
            getLogger().warning("============================");

            getLogger().severe(String.valueOf(getConfiguration().getBoolean("test.value")));
            getLogger().severe(String.valueOf(getConfiguration().getBoolean("command.features.message-joke")));

            getLogger().severe(String.valueOf(Inferris.getConfiguration().getSection("friends").getInt("page.size"))); //returns 0
            getLogger().severe(Inferris.getProperties().getProperty("debug.mode")); //returns true
            getLogger().severe(Inferris.getConfiguration().getString("database.user")); // returns true
        }else{
            ServerStateManager.setCurrentState(ServerState.NORMAL);
        }
    }

    @Override
    public void onDisable() {
        jedisPool.close();
    }

    public void createConfig() {
        try {
            if (!getDataFolder().exists()) {
                getLogger().info("Creating folder: " + getDataFolder().mkdir());
            }

            configFile = new File(getDataFolder(), "config.yml");

            if (!configFile.exists()) {
                FileOutputStream outputStream = new FileOutputStream(configFile); // Throws IOException
                InputStream in = getResourceAsStream("config.yml"); // This file must exist in the jar resources folder
                in.transferTo(outputStream); // Throws IOException
            }

            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createPermissionsConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs(); // create the Inferris folder if it does not exist
            }

            permissionsFile = new File(getDataFolder(), "permissions.yml");

            if (!permissionsFile.exists()) {
                InputStream defaultConfig = Inferris.class.getResourceAsStream("/permissions.yml");
                Files.copy(defaultConfig, permissionsFile.toPath());
            }
            permissionsConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(permissionsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createPlayersConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs(); // create the Inferris folder if it does not exist
            }

            playersFile = new File(getDataFolder(), "players.yml");

            if (!permissionsFile.exists()) {
                InputStream defaultConfig = Inferris.class.getResourceAsStream("/players.yml");
                Files.copy(defaultConfig, playersFile.toPath());
            }
            playersConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(playersFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getConfig() {
        File pluginFolder = new File("plugins/Inferris");
        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs(); // create the Inferris folder if it does not exist
        }

        configFile = new File(pluginFolder, "config.yml");
        if (!configFile.exists()) {
            try {
                InputStream defaultConfig = Inferris.class.getResourceAsStream("/config.yml");
                Files.copy(defaultConfig, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Properties otherProperties = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            otherProperties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // do something with the config file here
    }

    public void createProperties() {
        File pluginFolder = new File("plugins/Inferris");
        propertiesFile = new File(pluginFolder, "inferris.properties");

        if (!propertiesFile.exists()) {
            try {
                InputStream defaultConfig = Inferris.class.getResourceAsStream("/inferris.properties");
                Files.copy(defaultConfig, propertiesFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        properties = new Properties();

        try (InputStream inputStream = new FileInputStream(propertiesFile)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (OutputStream outputStream = new FileOutputStream(propertiesFile)) {
            properties.store(outputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static File getPropertiesFile() {
        return propertiesFile;
    }

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperties(Properties properties) {
        Inferris.properties = properties;
    }

    public static Configuration getPermissionsConfiguration() {
        return permissionsConfiguration;
    }

    public static File getPermissionsFile() {
        return permissionsFile;
    }

    public static File getPlayersFile() {
        return playersFile;
    }

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static Configuration getPlayersConfiguration() {
        return playersConfiguration;
    }

    public static Inferris getInstance() {
        return instance;
    }

    public static JedisPool getJedisPool() {
        return jedisPool;
    }

    public static void setPermissionsConfiguration(Configuration permissionsConfiguration) {
        Inferris.permissionsConfiguration = permissionsConfiguration;
    }

    public static void setPlayersConfiguration(Configuration playersConfiguration) {
        Inferris.playersConfiguration = playersConfiguration;
    }
}
