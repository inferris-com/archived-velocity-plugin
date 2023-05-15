package com.inferris;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.inferris.database.DatabasePool;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Initializer {

    private static Cache<UUID,String> playerRegistryCache;

    public Initializer(){

        playerRegistryCache = CacheBuilder.newBuilder().build();
    }

    public static void loadPlayerRegistry() {
        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement query = connection.prepareStatement("SELECT * FROM players")) {

            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()){
                String uuid = resultSet.getString("uuid");
                String username = resultSet.getString("username");
                playerRegistryCache.put(UUID.fromString(uuid), username);
            }

            Inferris.getInstance().getLogger().warning("Player registry loaded successfully");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static Cache<UUID, String> getPlayerRegistryCache() {
        return playerRegistryCache;
    }
}
