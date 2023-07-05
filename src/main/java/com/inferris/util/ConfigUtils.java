package com.inferris.util;

import com.inferris.Inferris;
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
    private static Configuration configuration;
    private static Properties properties;

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

    public static void reloadConfiguration(Types types) {
        switch (types) {
            case CONFIG -> reloadConfiguration(new File(dataFolder, "config.yml"), "config");
            case PERMISSIONS -> reloadConfiguration(new File(dataFolder, "permissions.yml"), "permissions");
            case PLAYERS -> reloadConfiguration(new File(dataFolder, "players.yml"), "players");
            case PROPERTIES -> reloadProperties(new File(dataFolder, "inferris.properties"), "inferris");
        }
    }

    private static void reloadConfiguration(File file, String name) {
        file = new File(dataFolder, name + ".yml");

        try {
            if (!file.exists()) {
                InputStream defaultConfig = Inferris.class.getResourceAsStream("/" + name + ".yml");
                Files.copy(defaultConfig, file.toPath());
            }
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void reloadProperties(File file, String name) {
        file = new File(dataFolder, name + ".properties");
        properties = new Properties();

        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
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
        PLAYERS,
        PROPERTIES
    }
}
