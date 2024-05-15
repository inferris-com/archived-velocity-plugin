package com.inferris.config;

public enum ConfigType {
    CONFIG("config.yml"),
    PERMISSIONS("permissions.yml"),
    PROPERTIES("inferris.properties"),
    DATABASE("database.properties");

    private final String fileName;

    ConfigType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
