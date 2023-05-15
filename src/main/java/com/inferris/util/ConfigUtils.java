package com.inferris.util;

import com.inferris.Inferris;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.nio.file.Files;
import java.util.Map;

public class ConfigUtils {
    private static final Inferris inferris = Inferris.getInstance();
    private static final File dataFolder = inferris.getDataFolder();
    private Configuration configuration;


    public static Configuration createConfigFile(File file, Configuration configuration, String name) {

        file = new File(dataFolder, name + ".yml");

        try {
            if (!file.exists()) {
                InputStream defaultConfig = Inferris.class.getResourceAsStream("/" + name + ".yml");

                Files.copy(defaultConfig, file.toPath());
            }
           return configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void reloadConfiguration(Types types){
        switch (types){
            case CONFIG -> reloadConfiguration(new File(dataFolder, "config.yml"), configuration, "config");
            case PERMISSIONS -> reloadConfiguration(new File(dataFolder, "permissions.yml"), configuration, "permissions");
            case PLAYERS -> reloadConfiguration(new File(dataFolder, "players.yml"), configuration, "players");
        }
    }

    private void reloadConfiguration(File file, Configuration configuration, String name) {
        this.configuration = configuration;

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

    public void saveConfiguration(File file, Configuration configuration){
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
        PLAYERS;
    }
}
