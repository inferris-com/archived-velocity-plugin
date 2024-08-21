package com.inferris.player.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.database.Table;
import com.inferris.player.PlayerData;
import com.inferris.util.DatabaseUtils;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FetchPlayer {

    /**
     * Retrieves the UUID associated with a given username from player data stored in Redis.
     *
     * @param username The username to search for.
     * @return The UUID corresponding to the provided username, or null if no match is found.
     */

    private final PlayerDataRepository playerDataRepository;

    @Inject
    public FetchPlayer(PlayerDataRepository playerDataRepository) {
        this.playerDataRepository = playerDataRepository;
    }

    public UUID getUUIDByUsername(String username) {
        UUID uuid;
        PlayerData playerData = null;

        try (Jedis jedis = Inferris.getJedisPool().getResource()) {
            String playerDataJson = jedis.hget("playerdata", username);

            if (playerDataJson != null) {
                JsonElement jsonElement = new Gson().fromJson(playerDataJson, JsonElement.class);
                String uuidStr = jsonElement.getAsJsonObject().get("uuid").getAsString();
                return UUID.fromString(uuidStr);
            } else {
                // Not found in Jedis, retrieve from the database
                String condition = "`username` = '" + username + "'";
                try (Connection connection = DatabasePool.getConnection()) {
                    ResultSet resultSet = DatabaseUtils.queryData(connection, Table.PLAYER_DATA.getName(), new String[]{"uuid"}, condition);
                    if (resultSet.next()) {
                        uuid = UUID.fromString(resultSet.getString("uuid"));
                        playerData = playerDataRepository.getPlayerDataFromDatabase(uuid);
                    }
                } catch (SQLException e) {
                    Inferris.getInstance().getLogger().severe(e.getMessage());
                }
                if (playerData != null) {
                    return playerData.getUuid();
                }
            }
            return null; // Not found in both Jedis and the database
        }
    }

    public boolean hasUUIDByUsername(String username) {
        UUID uuid = getUUIDByUsername(username);
        return uuid != null;
    }
}
