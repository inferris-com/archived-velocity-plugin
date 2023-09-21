package com.inferris.database;

import com.inferris.Inferris;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabasePool {
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePool.class);
    private static DatabaseConfigLoader configLoader;

    static {
        try{
            configLoader = new DatabaseConfigLoader();
        }catch(IOException e){
            e.printStackTrace();
        }
        config.addDataSourceProperty("dataSource.unreturnedConnectionTimeout", "30");
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        config.addDataSourceProperty("leakDetectionThreshold","true");
        LOGGER.info("Database pool initialized with maximum pool size {}", config.getMaximumPoolSize());
    }

    private DatabasePool() {}
    public static Connection getConnection() throws SQLException {
        // By default, connect to the "inferris" database
        config.setJdbcUrl(configLoader.getJdbcUrl(Database.INFERRIS.getType()));
        config.setUsername(configLoader.getUsername(Database.INFERRIS.getType()));
        config.setPassword(configLoader.getPassword(Database.INFERRIS.getType()));
        config.setMaximumPoolSize(configLoader.getMaxPoolSize(Database.INFERRIS.getType()));
        dataSource = new HikariDataSource(config);
        return dataSource.getConnection();
    }

    public static Connection getConnection(Database database) throws SQLException {
        //HikariConfig config = new HikariConfig();
        config.setJdbcUrl(configLoader.getJdbcUrl(database.getType()));
        config.setUsername(configLoader.getUsername(database.getType()));
        config.setPassword(configLoader.getPassword(database.getType()));
        config.setMaximumPoolSize(configLoader.getMaxPoolSize(database.getType()));
        // ... Other configuration properties

        dataSource = new HikariDataSource(config);

        LOGGER.info("Database pool initialized with maximum pool size {}", config.getMaximumPoolSize());

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
        HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
        LOGGER.info("Connections in use: {}", poolMXBean.getActiveConnections());
        LOGGER.info("Idle connections: {}", poolMXBean.getIdleConnections());
        LOGGER.info("Total connections: {}", poolMXBean.getTotalConnections());
    }

    public static int connections() {
        HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
        return poolMXBean.getActiveConnections();
    }

    public static int getNumOpenConnections() {
        return dataSource.getHikariPoolMXBean().getTotalConnections();
    }

    public static int getNumInUseConnections() {
        return dataSource.getHikariPoolMXBean().getActiveConnections();
    }

}