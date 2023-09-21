package com.inferris.database;

import com.inferris.Inferris;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

public class DatabaseConfigLoader {
    private final Properties properties;
    private final File pluginFolder = new File("plugins/Inferris");
    private final File propertiesFile = new File(pluginFolder, "database.properties");

    public DatabaseConfigLoader() throws IOException {
        properties = new Properties();
        loadProperties();
    }

    public void loadProperties() {
        if (!propertiesFile.exists()) {
            generateDefaultProperties();
        }


        try (InputStream inputStream = new FileInputStream(propertiesFile)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateDefaultProperties() {
        InputStream defaultConfig = Inferris.class.getResourceAsStream("/" + "database.properties");
        try {
            assert defaultConfig != null;
            Files.copy(defaultConfig, propertiesFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getJdbcUrl(String databaseName) {
        return properties.getProperty(databaseName + ".jdbcUrl");
    }

    public String getUsername(String databaseName) {
        return properties.getProperty(databaseName + ".username");
    }

    public String getPassword(String databaseName) {
        return properties.getProperty(databaseName + ".password");
    }

    public int getMaxPoolSize(String databaseName) {
        return Integer.parseInt(properties.getProperty(databaseName + ".maxPoolSize"));
    }
}
