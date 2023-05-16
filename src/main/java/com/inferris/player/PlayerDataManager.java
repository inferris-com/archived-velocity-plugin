package com.inferris.player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.inferris.Inferris;
import com.inferris.Initializer;
import com.inferris.database.DatabasePool;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDataManager {
    private static PlayerDataManager instance;
    private final Cache<UUID,PlayerData> playerDataCache;

    public PlayerDataManager(){
        playerDataCache = CacheBuilder.newBuilder().build();
    }

    public static synchronized PlayerDataManager getInstance(){
        if(instance == null){
            instance = new PlayerDataManager();
        }
        return instance;
    }

    public PlayerData getPlayerData(ProxiedPlayer player){
        PlayerData playerData = playerDataCache.getIfPresent(player.getUniqueId());
        if(playerData == null){
            playerData = new PlayerData(player);
            playerDataCache.put(player.getUniqueId(), playerData);
        }
        return playerData;
    }

    public void checkJoinedBefore(ProxiedPlayer player) {
        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?");
             PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO players (uuid, username, is_vanished) VALUES (?, ?, ?)");
             PreparedStatement updateStatement = connection.prepareStatement("UPDATE players SET username = ? WHERE uuid =?")) {

            queryStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = queryStatement.executeQuery();

            if(resultSet.next()){
                String storedUsername = resultSet.getString("username");

                Inferris.getInstance().getLogger().info("Properly in table");


                if(!player.getName().equalsIgnoreCase(storedUsername)){
                    updateStatement.setString(1, player.getName());
                    updateStatement.setString(2, player.getUniqueId().toString());
                    updateStatement.executeUpdate();
                    Inferris.getInstance().getLogger().info("Updated username");
                }
            }else{
                insertStatement.setString(1, player.getUniqueId().toString());
                insertStatement.setString(2, player.getName());
                insertStatement.setInt(3, 0);
                insertStatement.execute();
                Inferris.getInstance().getLogger().info("Added player to table");

            }

            com.github.benmanes.caffeine.cache.Cache<UUID, String> playerRegistryCache = Initializer.getPlayerRegistryCache();

            if(playerRegistryCache.asMap().containsKey(player.getUniqueId())){
                Inferris.getInstance().getLogger().info("Registry contains uuid");
                String cachedUsername = playerRegistryCache.getIfPresent(player.getUniqueId());
                if(!player.getName().equals(cachedUsername)){
                    Inferris.getInstance().getLogger().warning("Registry contains uuid but username changed, invalidating and caching");

                    playerRegistryCache.invalidate(player.getUniqueId());
                    playerRegistryCache.put(player.getUniqueId(), player.getName());
                }
            }else{
                Inferris.getInstance().getLogger().info("Registry record not found, so I'm adding to registry");

                playerRegistryCache.put(player.getUniqueId(), player.getName());
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void invalidatePlayerData(ProxiedPlayer player){
        playerDataCache.invalidate(player.getUniqueId());
    }

    public Cache<UUID, PlayerData> getPlayerDataCache() {
        return playerDataCache;
    }
}
