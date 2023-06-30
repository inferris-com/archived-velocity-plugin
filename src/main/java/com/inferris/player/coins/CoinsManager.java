package com.inferris.player.coins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.server.JedisChannels;
import com.inferris.util.CacheSerializationUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CoinsManager {
    public static void setCoins(ProxiedPlayer player, int amount){
        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement("UPDATE players SET coins = ? WHERE uuid = ?")){
            updateStatement.setInt(1, amount);
            updateStatement.setString(2, player.getUniqueId().toString());
            updateStatement.executeUpdate();

        }catch(SQLException e){
            e.printStackTrace();
        }

        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
        playerData.setCoins(amount);

        try(Jedis jedis = Inferris.getJedisPool().getResource()){
            String json = CacheSerializationUtils.serializePlayerData(playerData);
            jedis.hset("playerdata", player.getUniqueId().toString(), json);
            jedis.publish(JedisChannels.PLAYERDATA_COINS.name(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
