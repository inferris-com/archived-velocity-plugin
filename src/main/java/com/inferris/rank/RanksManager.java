package com.inferris.rank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.registry.RegistryManager;
import com.inferris.server.JedisChannels;
import com.inferris.util.CacheSerializationUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RanksManager {
    private static RanksManager instance;
    private final JedisPool jedisPool;

    private RanksManager() {
        jedisPool = Inferris.getJedisPool(); // Set Redis server details
    }

    public static RanksManager getInstance() {
        if (instance == null) {
            instance = new RanksManager();
        }
        return instance;
    }

    public Rank getRank(ProxiedPlayer player) {
        return loadRanks(player);
    }

    public Rank loadRanks(ProxiedPlayer player) {
        Inferris.getInstance().getLogger().warning("Loading ranks");
        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT staff, builder, donor, other FROM ranks WHERE `uuid` = ?");
             PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO ranks (uuid, staff, builder, donor, other) VALUES (?,?,?,?,?)")) {
            statement.setString(1, String.valueOf(player.getUniqueId()));
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int staff = rs.getInt("staff");
                int builder = rs.getInt("builder");
                int donor = rs.getInt("donor");
                int other = rs.getInt("other");
                return new Rank(staff, builder, donor, other);
            } else {
                insertStatement.setString(1, String.valueOf(player.getUniqueId()));
                insertStatement.setInt(2, 0); // Default value for staff rank
                insertStatement.setInt(3, 0); // Default value for builder rank
                insertStatement.setInt(4, 0); // Default value for donor rank
                insertStatement.setInt(5, 0); // Default value for other rank
                insertStatement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Rank(0, 0, 0, 0);
    }

    public void setRank(UUID uuid, Branch branch, int id) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        boolean isNull = player == null;

        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM ranks WHERE uuid = ?");
             PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO ranks (uuid, staff, builder, donor, other) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement updateStatement = connection.prepareStatement("UPDATE ranks SET staff = ?, builder = ?, donor = ?, other = ? WHERE uuid = ?")) {

            // Check if player exists in ranks table
            queryStatement.setString(1, uuid.toString());
            ResultSet resultSet = queryStatement.executeQuery();

            if (resultSet.next()) {
                // Player exists, update their rank
                int currentStaff = resultSet.getInt("staff");
                int currentBuilder = resultSet.getInt("builder");
                int currentDonor = resultSet.getInt("donor");
                int currentOther = resultSet.getInt("other");

                // Set the values for the update statement
                updateStatement.setInt(1, branch == Branch.STAFF ? id : currentStaff);
                updateStatement.setInt(2, branch == Branch.BUILDER ? id : currentBuilder);
                updateStatement.setInt(3, branch == Branch.DONOR ? id : currentDonor);
                updateStatement.setInt(4, branch == Branch.OTHER ? id : currentOther);
                updateStatement.setString(5, uuid.toString());
                updateStatement.executeUpdate();
            } else {
                // Player does not exist, insert their rank
                insertStatement.setString(1, uuid.toString());
                insertStatement.setInt(2, 0);
                insertStatement.setInt(3, 0);
                insertStatement.setInt(4, 0);
                insertStatement.setInt(5, 0);
                insertStatement.executeUpdate();
            }

            // Update the cached rank for the player
            PlayerData playerData = PlayerDataManager.getInstance().getRedisDataOrNull(uuid);
            //Rank rank = getRank(player);
            Rank rank = playerData.getRank();
            switch (branch) {
                case STAFF -> {
                    rank.setStaff(id);
                    break;
                }
                case BUILDER -> {
                    rank.setBuilder(id);
                    break;
                }
                case DONOR -> {
                    rank.setDonor(id);
                    break;
                }
                case OTHER -> {
                    rank.setOther(id);
                    break;

                }
                default -> {
                    Inferris.getInstance().getLogger().warning("Invalid rank branch specified");
                    return;
                }
            }

            /*
            Caching, updating, and Jedis publishing
             */

            try (Jedis jedis = jedisPool.getResource()) {
                String json = CacheSerializationUtils.serializePlayerData(playerData);
                jedis.hset("playerdata", uuid.toString(), json);
                if (!isNull) {
                    PlayerDataManager.getInstance().updateAllData(player, playerData);
                    String payload = playerData.getCurrentServer().name() + ":" + json;
                    jedis.publish(JedisChannels.PLAYERDATA_UPDATE.getChannelName(), payload);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            return;
        }
    }

    private Rank createEmpty(ProxiedPlayer player) {
        // Create and return an empty Registry object with default values
        return new Rank(0, 0, 0, 0);
    }

    public void invalidateEntry() {
        try (Jedis jedis = RegistryManager.getInstance().getJedisPool().getResource()) {
            jedis.del("playerdata");
        }
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}
