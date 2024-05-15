package com.inferris.config;

import com.inferris.Inferris;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigurationHandler {
    private static ConfigurationHandler instance;
    private final Map<ConfigType, Configuration> configurations = new HashMap<>();
    private final Map<ConfigType, File> configFiles = new HashMap<>();
    private final Map<ConfigType, Properties> propertiesFiles = new HashMap<>();

    private ConfigurationHandler() {}

    public static ConfigurationHandler getInstance() {
        if (instance == null) {
            instance = new ConfigurationHandler();
        }
        return instance;
    }

    // Load YAML Configuration
    public void loadConfig(ConfigType type) throws IOException {
        File configFile = new File(Inferris.getInstance().getDataFolder(), type.getFileName());
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try (InputStream in = Inferris.getInstance().getResourceAsStream(type.getFileName())) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                }
            }
        }
        Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        configurations.put(type, config);
        configFiles.put(type, configFile);
    }

    public Configuration getConfig(ConfigType type) {
        return configurations.get(type);
    }

    public String getConfigValue(ConfigType type, String path) {
        return getConfig(type).getString(path);
    }

    public void saveConfig(ConfigType type) throws IOException {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(configurations.get(type), configFiles.get(type));
    }

    public void reloadConfig(ConfigType type) throws IOException {
        File configFile = configFiles.get(type);
        configurations.put(type, YamlConfiguration.getProvider(YamlConfiguration.class).load(configFile));
    }

    // Load Properties Configuration
    public void loadProperties(ConfigType type) {
        File propertiesFile = new File("plugins/Inferris", type.getFileName());
        if (!propertiesFile.exists()) {
            propertiesFile.getParentFile().mkdirs();
            try (InputStream defaultConfig = Inferris.class.getResourceAsStream("/" + type.getFileName())) {
                if (defaultConfig != null) {
                    Files.copy(defaultConfig, propertiesFile.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (InputStream in = new FileInputStream(propertiesFile)) {
            Properties props = new Properties();
            props.load(in);
            propertiesFiles.put(type, props);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Properties getProperties(ConfigType type) {
        return propertiesFiles.get(type);
    }

    public void saveProperties(ConfigType type) {
        try (OutputStream out = new FileOutputStream(new File("plugins/Inferris", type.getFileName()))) {
            propertiesFiles.get(type).store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadProperties(ConfigType type) {
        loadProperties(type);
    }
}