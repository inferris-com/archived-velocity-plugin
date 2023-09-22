package com.inferris.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabasePool {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePool.class);
    private static final Map<Database, HikariDataSource> dataSources = new HashMap<>();
    private static DatabaseConfigLoader configLoader;

    static {
        try {
            configLoader = new DatabaseConfigLoader();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize connection pools for each database type
        for (Database database : Database.values()) {
            HikariConfig config = new HikariConfig();
            config.addDataSourceProperty("dataSource.unreturnedConnectionTimeout", "30");
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("leakDetectionThreshold", "true");
            config.setJdbcUrl(configLoader.getJdbcUrl(database.getType()));
            config.setUsername(configLoader.getUsername(database.getType()));
            config.setPassword(configLoader.getPassword(database.getType()));
            config.setMaximumPoolSize(configLoader.getMaxPoolSize(database.getType()));

            HikariDataSource dataSource = new HikariDataSource(config);
            dataSources.put(database, dataSource);

            LOGGER.info("Database pool initialized with maximum pool size {} for database: {}",
                    config.getMaximumPoolSize(), database);
        }
    }

    private DatabasePool() {
    }

    public static Connection getConnection() throws SQLException {
        // Get a connection from the appropriate connection pool based on the database type
        HikariDataSource dataSource = dataSources.get(Database.INFERRIS);
        if (dataSource == null) {
            throw new IllegalArgumentException("Unsupported database type: " + Database.INFERRIS);
        }
        return dataSource.getConnection();
    }

    public static Connection getConnection(Database database) throws SQLException {
        // Get a connection from the appropriate connection pool based on the database type
        HikariDataSource dataSource = dataSources.get(database);
        if (dataSource == null) {
            throw new IllegalArgumentException("Unsupported database type: " + database);
        }
        return dataSource.getConnection();
    }

    public static void closeConnection(Connection connection) {
        try {
            connection.close();
            LOGGER.debug("Connection {} closed", connection);
        } catch (SQLException e) {
            LOGGER.error("Error closing connection", e);
        }
    }

    public static void listConnections() {
        HikariPoolMXBean poolMXBean = dataSources.get(Database.INFERRIS).getHikariPoolMXBean();
        LOGGER.info("Connections in use: {}", poolMXBean.getActiveConnections());
        LOGGER.info("Idle connections: {}", poolMXBean.getIdleConnections());
        LOGGER.info("Total connections: {}", poolMXBean.getTotalConnections());
    }

    public static int connections() {
        HikariPoolMXBean poolMXBean = dataSources.get(Database.INFERRIS).getHikariPoolMXBean();
        return poolMXBean.getActiveConnections();
    }

    public static int getNumOpenConnections() {
        return dataSources.get(Database.INFERRIS).getHikariPoolMXBean().getTotalConnections();
    }

    public static int getNumInUseConnections() {
        return dataSources.get(Database.INFERRIS).getHikariPoolMXBean().getActiveConnections();
    }

}