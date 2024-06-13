package com.inferris.player.service;

import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.player.PlayerData;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.server.jedis.JedisChannel;
import com.inferris.util.DatabaseUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RanksManager {
    private static RanksManager instance;

    private RanksManager() {
    }

    public static RanksManager getInstance() {
        if (instance == null) {
            instance = new RanksManager();
        }
        return instance;
    }

    public Rank loadRanks(UUID uuid, Connection connection) {
        Inferris.getInstance().getLogger().warning("Loading ranks");
        try (PreparedStatement statement = connection.prepareStatement("SELECT staff, builder, donor, other FROM `rank` WHERE `uuid` = ?")) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                int staff = rs.getInt("staff");
                int builder = rs.getInt("builder");
                int donor = rs.getInt("donor");
                int other = rs.getInt("other");
                return new Rank(staff, builder, donor, other);
            } else {
                String[] columnNames = {"uuid", "staff", "builder", "donor", "other"};
                Object[] values = {uuid.toString(), 0, 0, 0, 0};

                DatabaseUtils.insertData(connection, "`rank`", columnNames, values);

                Inferris.getInstance().getLogger().info("Loading ranks, inserting");
            }
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().severe("Fatal error with loading ranks: " + e.getMessage());
        }
        return new Rank(0, 0, 0, 0);
    }

    public void setRank(UUID uuid, Branch branch, int id) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        boolean isNull = player == null;

        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM `rank` WHERE uuid = ?");
             PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO `rank` (uuid, staff, builder, donor, other) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement updateStatement = connection.prepareStatement("UPDATE `rank` SET staff = ?, builder = ?, donor = ?, other = ? WHERE uuid = ?")) {

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
                }
                case BUILDER -> {
                    rank.setBuilder(id);
                }
                case DONOR -> {
                    rank.setDonor(id);
                }
                case OTHER -> {
                    rank.setOther(id);
                }
                default -> {
                    Inferris.getInstance().getLogger().warning("Invalid rank branch specified");
                    return;
                }
            }
            // Update Player Data and push
            if (!isNull) {
                PlayerDataManager.getInstance().updateAllDataAndPush(player, playerData, JedisChannel.PLAYERDATA_UPDATE);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
