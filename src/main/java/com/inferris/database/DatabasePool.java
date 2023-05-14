package com.inferris.database;

import com.inferris.Inferris;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabasePool {
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(DatabasePool.class);

    static {
        String address = Inferris.getConfiguration().getString("database.address");
        String database = Inferris.getConfiguration().getString("database.database");
        config.setJdbcUrl("jdbc:mysql://" + address + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true");
        config.setUsername(Inferris.getConfiguration().getString("database.user"));
        config.setPassword(Inferris.getConfiguration().getString("database.password"));
        config.setMaximumPoolSize(5); // set maximum number of connections
        config.addDataSourceProperty("dataSource.unreturnedConnectionTimeout", "30");
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        config.addDataSourceProperty("leakDetectionThreshold","true");
        dataSource = new HikariDataSource(config);
        logger.info("Database pool initialized with maximum pool size {}", config.getMaximumPoolSize());
    }

    private DatabasePool() {}
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeConnection(Connection connection) {
        try {
            connection.close();
            logger.debug("Connection {} closed", connection);
        } catch (SQLException e) {
            logger.error("Error closing connection", e);
        }
    }

    public static void listConnections() {
        HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
        logger.info("Connections in use: {}", poolMXBean.getActiveConnections());
        logger.info("Idle connections: {}", poolMXBean.getIdleConnections());
        logger.info("Total connections: {}", poolMXBean.getTotalConnections());
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