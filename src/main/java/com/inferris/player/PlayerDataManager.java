package com.inferris.player;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inferris.Channels;
import com.inferris.Inferris;
import com.inferris.Registry;
import com.inferris.RegistryManager;
import com.inferris.database.DatabasePool;
import com.inferris.util.ConfigUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PlayerDataManager {
    private static PlayerDataManager instance;
    private final Cache<UUID,PlayerData> playerDataCache;

    private PlayerDataManager(){
        playerDataCache = Caffeine.newBuilder().build();
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
        Inferris.getInstance().getLogger().warning("Checking join");

        Cache<UUID, Registry> registryCache = RegistryManager.getPlayerRegistryCache();
        Registry registry = RegistryManager.getInstance().getRegistry(player);

        String username = registry.getUsername();

        if(registryCache.getIfPresent(player.getUniqueId()) != null && player.getName().equalsIgnoreCase(username)){
            Inferris.getInstance().getLogger().warning("In registr");
        }else{
            registryCache.put(player.getUniqueId(), new Registry(player.getUniqueId(), player.getName(), Channels.valueOf(Channels.NONE.getMessage())));
            Inferris.getInstance().getLogger().warning("Not in registry, caching");

        }

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
                    Inferris.getInstance().getLogger().warning("Updated username");
                    Channels channel = registry.getChannel();

                    registryCache.invalidate(player.getUniqueId());
                    registryCache.put(player.getUniqueId(), new Registry(player.getUniqueId(), player.getName(), channel));
                }
            }else{
                Inferris.getInstance().getLogger().warning("Inserting into table.");

                insertStatement.setString(1, player.getUniqueId().toString());
                insertStatement.setString(2, player.getName());
                insertStatement.setInt(3, 0);
                insertStatement.execute();

                Inferris.getInstance().getLogger().severe("Added player to table");
                registryCache.invalidate(player.getUniqueId());
                registryCache.put(player.getUniqueId(), new Registry(player.getUniqueId(), player.getName(), Channels.valueOf(Channels.NONE.getMessage())));

                Inferris.getPlayersConfiguration().set("players." + player.getUniqueId() + "." + "channel", Channels.valueOf(Channels.NONE.getMessage()));

                ConfigUtils configUtils = new ConfigUtils();
                configUtils.saveConfiguration(Inferris.getPlayersFile(), Inferris.getPlayersConfiguration());
                configUtils.reloadConfiguration(ConfigUtils.Types.PLAYERS);
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
