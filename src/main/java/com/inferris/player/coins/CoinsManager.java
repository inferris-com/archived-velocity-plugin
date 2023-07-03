package com.inferris.player.coins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.server.JedisChannels;
import com.inferris.util.CacheSerializationUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class CoinsManager {
    public static void setCoins(UUID uuid, int amount){
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        boolean isNull = player == null;
        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement updateStatement = connection.prepareStatement("UPDATE players SET coins = ? WHERE uuid = ?")){
            updateStatement.setInt(1, amount);
            updateStatement.setString(2, uuid.toString());
            updateStatement.executeUpdate();

        }catch(SQLException e){
            e.printStackTrace();
        }

        PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(uuid);
        playerData.getCoins().setBalance(amount);

        try(Jedis jedis = Inferris.getJedisPool().getResource()){
            String json = CacheSerializationUtils.serializePlayerData(playerData);
            jedis.hset("playerdata", uuid.toString(), json);
            if(!isNull) {
                PlayerDataManager.getInstance().updateAllData(player, playerData);

                jedis.publish(JedisChannels.PROXY_TO_SPIGOT_PLAYERDATA_CACHE_UPDATE.name(), json);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
