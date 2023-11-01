package com.inferris.util;

import com.inferris.database.Database;
import com.inferris.database.DatabasePool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseUtils {

    public static ResultSet executeQuery(Connection connection, String tableName, String[] columnNames, String condition, Object... parameters) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT ");
        if (columnNames == null || columnNames.length == 0) {
            sb.append("*");
        } else {
            for (int i = 0; i < columnNames.length; i++) {
                sb.append(columnNames[i]);
                if (i < columnNames.length - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append(" FROM ").append(tableName);
        if (condition != null && !condition.isEmpty()) {
            sb.append(" WHERE ").append(condition);
        }
        String sql = sb.toString();

        PreparedStatement statement = connection.prepareStatement(sql);

        // Set parameters dynamically
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }

        return statement.executeQuery();
    }



    public static ResultSet executeQuery(String sql, Object... parameters) throws SQLException {
        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set parameters dynamically
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            return statement.executeQuery();
        }
    }



    public static int executeUpdate(String sql, Object... parameters) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        int affectedRows = 0;

        try {
            connection = DatabasePool.getConnection();
            statement = connection.prepareStatement(sql);

            // Set parameters dynamically
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            affectedRows = statement.executeUpdate();
        } catch (SQLException e) {
            // Handle the exception appropriately
            throw e;
        } finally {
            // Close resources in the reverse order of their creation
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }

        return affectedRows;
    }

    public static void insertData(Connection connection, String tableName, String[] columnNames, Object[] values) throws SQLException {
        String sql = buildInsertQuery(tableName, columnNames, values);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            statement.executeUpdate();
        }
    }

    private static String buildInsertQuery(String tableName, String[] columnNames, Object[] values) {
        StringBuilder sb = new StringBuilder("INSERT INTO " + tableName + " (");
        for (int i = 0; i < columnNames.length; i++) {
            sb.append(columnNames[i]);
            if (i < columnNames.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(") VALUES (");
        for (int i = 0; i < values.length; i++) {
            sb.append("?");
            if (i < values.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private static String buildUpdateQuery(String tableName, String[] columnNames, String whereClause) {
        StringBuilder sb = new StringBuilder("UPDATE " + tableName + " SET ");
        for (int i = 0; i < columnNames.length; i++) {
            sb.append(columnNames[i]).append(" = ?");
            if (i < columnNames.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(" WHERE ").append(whereClause);
        return sb.toString();
    }

    public static ResultSet queryData(Connection connection, String tableName, String[] columnNames, String condition) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT ");
        if (columnNames == null || columnNames.length == 0) {
            sb.append("*");
        } else {
            for (int i = 0; i < columnNames.length; i++) {
                sb.append(columnNames[i]);
                if (i < columnNames.length - 1) {
                    sb.append(", ");
                }
            }
        }
        sb.append(" FROM ").append(tableName);
        if (condition != null && !condition.isEmpty()) {
            sb.append(" WHERE ").append(condition);
        }
        String sql = sb.toString();

        PreparedStatement statement = connection.prepareStatement(sql);

        return statement.executeQuery();
    }

    public static void removeData(Connection connection, String tableName, String condition) throws SQLException {
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        sb.append(tableName);
        if (condition != null && !condition.isEmpty()) {
            sb.append(" WHERE ").append(condition);
        }
        String sql = sb.toString();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }
}

