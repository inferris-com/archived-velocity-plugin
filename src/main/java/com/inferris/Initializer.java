package com.inferris;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inferris.database.DatabasePool;
import net.md_5.bungee.config.Configuration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Initializer {
    RegistryManager registryManager;

    public Initializer(){
        registryManager = RegistryManager.getInstance();
    }

    public void loadPlayerRegistry() {
        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement query = connection.prepareStatement("SELECT * FROM players")) {

            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()){
                String uuid = resultSet.getString("uuid");
                String username = resultSet.getString("username");
                Configuration playersConfiguration = Inferris.getPlayersConfiguration();
                Channels channel = Channels.valueOf(playersConfiguration.getString("players." + uuid + ".channel"));
                RegistryManager.getPlayerRegistryCache().put(UUID.fromString(uuid), new Registry(UUID.fromString(uuid), username, channel));
            }

            Inferris.getInstance().getLogger().warning("Player registry loaded successfully");
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}
