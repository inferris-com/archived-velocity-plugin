package com.inferris.util;

import com.inferris.database.DatabasePool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseUtils {

    public static ResultSet executeQuery(String sql, Object... parameters) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabasePool.getConnection();
            statement = connection.prepareStatement(sql);

            // Set parameters dynamically
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }

            resultSet = statement.executeQuery();
            return resultSet;
        } catch (SQLException e) {
            // Handle exceptions
            throw e;
        } finally {
            // Close resources in reverse order
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
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

}

