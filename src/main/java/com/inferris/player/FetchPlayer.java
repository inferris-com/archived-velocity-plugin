package com.inferris.player;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.database.Table;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Rank;
import com.inferris.rank.RanksManager;
import com.inferris.server.Server;
import com.inferris.util.DatabaseUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class FetchPlayer {
    private final PlayerDataService playerDataService;
    private final PlayerDataRepository playerDataRepository;
    public FetchPlayer(PlayerDataService playerDataService, PlayerDataRepository playerDataRepository){
        this.playerDataService = playerDataService;
        this.playerDataRepository = playerDataRepository;
    }

    /**
     * Retrieves the UUID associated with a given username from player data stored in Redis.
     *
     * @param username The username to search for.
     * @return The UUID corresponding to the provided username, or null if no match is found.
     */
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
