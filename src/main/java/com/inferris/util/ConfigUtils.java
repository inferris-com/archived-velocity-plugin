package com.inferris.util;

import com.inferris.Inferris;
import com.inferris.config.ConfigType;
import com.inferris.config.ConfigurationHandler;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;

public class ConfigUtils {
    private static final Inferris inferris = Inferris.getInstance();
    private static final File dataFolder = inferris.getDataFolder();
    private static final ConfigurationHandler handler = Inferris.getInstance().getConfigurationHandler();
    private final Properties PROPERTIES_FILE = handler.getProperties(ConfigType.PROPERTIES);
    private final Configuration PERMISSIONS_FILE = handler.getConfig(ConfigType.PERMISSIONS);

    private final Configuration CONFIG_FILE = handler.getConfig(ConfigType.CONFIG);

    public static Configuration createConfigFile(File file, String name) {
        file = new File(dataFolder, name + ".yml");

        try {
            if (!file.exists()) {
                InputStream defaultConfig = Inferris.class.getResourceAsStream("/" + name + ".yml");
                Files.copy(defaultConfig, file.toPath());
            }
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reloadConfiguration(Types types) throws IOException {
        switch (types){
            case CONFIG -> handler.reloadConfig(ConfigType.CONFIG);
            case PERMISSIONS -> handler.reloadConfig(ConfigType.PERMISSIONS);
            case PROPERTIES -> handler.reloadConfig(ConfigType.PROPERTIES);
            case DATABASE -> handler.reloadConfig(ConfigType.DATABASE);
        }
    }


    public static void saveConfiguration(File file, Configuration configuration) {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getNestedValue(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object value = data.get(key);
            if (value == null) {
                return null;
            } else if (value instanceof Map) {
                data = (Map<String, Object>) value;
            } else {
                return value;
            }
        }
        return null;
    }

    public enum Types {
        CONFIG,
        PERMISSIONS,
        PROPERTIES,
        DATABASE
    }
}
