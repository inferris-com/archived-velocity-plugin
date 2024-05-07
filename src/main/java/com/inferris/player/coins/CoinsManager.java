package com.inferris.player.coins;

import com.inferris.database.DatabasePool;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class CoinsManager {
    public static void setCoins(UUID uuid, int amount) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        boolean isNull = player == null;
        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement("UPDATE player_data SET coins = ? WHERE uuid = ?")) {
            updateStatement.setInt(1, amount);
            updateStatement.setString(2, uuid.toString());
            updateStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(uuid);
        playerData.getCoins().setBalance(amount);

        //jedis.hset("playerdata", uuid.toString(), json);
        if (!isNull) {
            PlayerDataManager.getInstance().updateAllDataAndPush(player, playerData);
        }
    }
}
