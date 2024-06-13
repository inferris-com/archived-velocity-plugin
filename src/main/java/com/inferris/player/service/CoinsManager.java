package com.inferris.player.service;

import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.player.ServiceLocator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoinsManager {
    private static CoinsManager instance;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private CoinsManager(){}

    public static synchronized CoinsManager getInstance(){
        if(instance == null){
            instance = new CoinsManager();
        }
        return instance;
    }

    public void setCoins(UUID uuid, int amount) {
        try {
            setCoinsAsync(uuid, amount).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> setCoinsAsync(UUID uuid, int amount) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = DatabasePool.getConnection();
                 PreparedStatement updateStatement = connection.prepareStatement("UPDATE player_data SET coins = ? WHERE uuid = ?")) {
                updateStatement.setInt(1, amount);
                updateStatement.setString(2, uuid.toString());
                updateStatement.executeUpdate();

                ServiceLocator.getPlayerDataService().updatePlayerData(uuid, pd ->
                        pd.setCoins(amount));
            } catch (SQLException e) {
                Inferris.getInstance().getLogger().severe("SQLException from CoinsManager: " + e.getMessage());
            }
        }, executorService);
    }
}