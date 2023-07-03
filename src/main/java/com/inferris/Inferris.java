package com.inferris;

import com.inferris.commands.*;
import com.inferris.database.DatabasePool;
import com.inferris.events.EventJoin;
import com.inferris.events.EventPing;
import com.inferris.events.EventQuit;
import com.inferris.events.EventReceive;
import com.inferris.server.BungeeChannel;
import com.inferris.server.Initializer;
import com.inferris.server.Ports;
import com.inferris.util.ConfigUtils;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import redis.clients.jedis.JedisPool;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

public class Inferris extends Plugin {
    private static Inferris instance;
    private Path dataDirectory;
    private static Properties properties;
    private static File configFile;
    private static File permissionsFile;
    private static File playersFile;
    private static File propertiesFile;
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

        //PlayerDataManager playerDataManager = PlayerDataManager.getInstance();

        PluginManager pluginManager = getProxy().getPluginManager();
        pluginManager.registerListener(this, new EventJoin());
        pluginManager.registerListener(this, new EventQuit());
        pluginManager.registerListener(this, new EventReceive());
        pluginManager.registerListener(this, new EventPing());
        pluginManager.registerCommand(this, new CommandTest("bungeetest"));
        pluginManager.registerCommand(this, new CommandConfig("config"));
        pluginManager.registerCommand(this, new CommandMessage("message"));
        pluginManager.registerCommand(this, new CommandReply("reply"));
        pluginManager.registerCommand(this, new CommandChannel("channel"));
        //pluginManager.registerCommand(this, new CommandMessage("message"));
        //pluginManager.registerCommand(this, new CommandReply("reply")); odo
        pluginManager.registerCommand(this, new CommandVanish("vanish"));
        pluginManager.registerCommand(this, new CommandSetrank("rank"));
        pluginManager.registerCommand(this, new CommandCoins("coins"));
        pluginManager.registerCommand(this, new CommandProfile("profile"));
        pluginManager.registerCommand(this, new CommandAccount("account"));

        getProxy().registerChannel(BungeeChannel.STAFFCHAT.getName());
        getProxy().registerChannel(BungeeChannel.PLAYER_REGISTRY.getName());

        try {
            Connection connection = DatabasePool.getConnection();
            if (connection.isClosed()) {
                getLogger().log(Level.SEVERE, "Database connection is closed!");
            } else {
                getLogger().log(Level.WARNING, "Connected to database!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        playersFile = new File(getDataFolder(), "players.yml");
        playersConfiguration = ConfigUtils.createConfigFile(playersFile, playersConfiguration, "players");

        Initializer initializer = new Initializer();
        //initializer.loadPlayerRegistry();
        jedisPool = new JedisPool("localhost", Ports.JEDIS.getPort());

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

        try (InputStream inputStream = new FileInputStream((propertiesFile))) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
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

        properties = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // do something with the config file here
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
