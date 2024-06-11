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
    public FetchPlayer(PlayerDataService playerDataService){
        this.playerDataService = playerDataService;
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
                        playerData = getPlayerDataFromDatabase(uuid);
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

    public PlayerData getPlayerDataFromDatabase(UUID uuid) {
        PlayerData playerData = null;
        Profile profile = null;
        String username;

        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM " + Table.PLAYER_DATA.getName() + " WHERE uuid = ?")) {

            queryStatement.setString(1, uuid.toString());
            ResultSet resultSet = queryStatement.executeQuery();

            if (resultSet.next()) {
                username = resultSet.getString("username");
                int coins = resultSet.getInt("coins");
                Channel channel = Channel.valueOf(resultSet.getString("channel"));
                int vanished = resultSet.getInt("vanished");

                VanishState vanishState;

                if (vanished == 1) {
                    vanishState = VanishState.ENABLED;
                } else {
                    vanishState = VanishState.DISABLED;
                }

                // Load ranks
                Rank rank = RanksManager.getInstance().loadRanks(uuid, connection);

                if (ProxyServer.getInstance().getPlayer(uuid) != null) {
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                    if (!username.equals(player.getName())) {
                        username = player.getName();
                        playerDataService.updatePlayerDataWithoutPush(player.getUniqueId(), pdToUpdate -> {
                            pdToUpdate.setUsername(player.getName());
                        });
                    }
                }

                // Query for profile data
                Long registrationDate = null;
                String bio = null;
                String pronouns = null;
                int xenforoId = 0;
                int discordLinked = 0;
                boolean isDiscordLinked = false;
                int flaggedInt = 0;
                boolean isFlagged = false;

                PreparedStatement selectProfileStatement = connection.prepareStatement("SELECT * FROM " + Table.PROFILE.getName() + " WHERE uuid = ?");
                selectProfileStatement.setString(1, uuid.toString());
                ResultSet profileResultSet = selectProfileStatement.executeQuery();

                if (profileResultSet.next()) {
                    registrationDate = profileResultSet.getLong("join_date");
                    bio = profileResultSet.getString("bio");
                    pronouns = profileResultSet.getString("pronouns");
                    xenforoId = profileResultSet.getInt("xenforo_id"); // Replace with the actual column name
                    discordLinked = profileResultSet.getInt("discord_linked");
                    flaggedInt = profileResultSet.getInt("is_flagged");

                    if (discordLinked != 0) {
                        isDiscordLinked = true;
                    }
                    isFlagged = (flaggedInt != 0);

                    profile = new Profile(registrationDate, bio, pronouns, xenforoId, isDiscordLinked, isFlagged);
                }

                playerData = new PlayerData(uuid, username, rank, profile, coins, channel, vanishState, Server.LOBBY);
            }
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().warning(e.getMessage());
        }

        return playerData;
    }

    // PlayerData. It returns null if not in database
    // PlayerData playerData = getPlayerDataFromDatabase(player.getUniqueId(), player.getName(), true);
    // todo, return type
    public PlayerData getPlayerDataFromDatabase(UUID uuid, String username, boolean insertData) {
        PlayerData playerData = null;
        Profile profile = null;

        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM " + Table.PLAYER_DATA.getName() + " WHERE uuid = ?")) {

            queryStatement.setString(1, uuid.toString());
            ResultSet resultSet = queryStatement.executeQuery();

            if (resultSet.next()) {
                username = resultSet.getString("username");
                int coins = resultSet.getInt("coins");
                Channel channel = Channel.valueOf(resultSet.getString("channel"));
                int vanished = resultSet.getInt("vanished");

                VanishState vanishState;

                if (vanished == 1) {
                    vanishState = VanishState.ENABLED;
                } else {
                    vanishState = VanishState.DISABLED;
                }

                Rank rank = RanksManager.getInstance().loadRanks(uuid, connection);

                if (ProxyServer.getInstance().getPlayer(uuid) != null) {
                    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
                    if (!username.equals(player.getName())) {
                        username = player.getName();
                        playerDataService.updatePlayerDataWithoutPush(player.getUniqueId(), pdToUpdate -> {
                            pdToUpdate.setUsername(player.getName());
                        });
                    }
                }

                // Query for profile data
                Long registrationDate = null;
                String bio = null;
                String pronouns = null;
                int xenforoId = 0;
                int discordLinked = 0;
                boolean isDiscordLinked = false;
                int flaggedInt = 0;
                boolean isFlagged = false;

                PreparedStatement selectProfileStatement = connection.prepareStatement("SELECT * FROM " + Table.PROFILE.getName() + " WHERE uuid = ?");
                selectProfileStatement.setString(1, uuid.toString());
                ResultSet profileResultSet = selectProfileStatement.executeQuery();

                if (profileResultSet.next()) {
                    registrationDate = profileResultSet.getLong("join_date");
                    bio = profileResultSet.getString("bio");
                    pronouns = profileResultSet.getString("pronouns");
                    xenforoId = profileResultSet.getInt("xenforo_id"); // Replace with the actual column name
                    discordLinked = profileResultSet.getInt("discord_linked");
                    flaggedInt = profileResultSet.getInt("is_flagged");

                    if (discordLinked != 0) {
                        isDiscordLinked = true;
                    }

                    isFlagged = (flaggedInt != 0);

                    profile = new Profile(registrationDate, bio, pronouns, xenforoId, isDiscordLinked, isFlagged);
                }
                playerData = new PlayerData(uuid, username, rank, profile, coins, channel, vanishState, Server.LOBBY);
            } else {
                // Insert into database if insertData is true
                if (insertData) {
                    PlayerDataManager.getInstance().insertPlayerDataToDatabase(connection, uuid, username);
                    playerData = playerDataService.getPlayerData(uuid);
                    Inferris.getInstance().getLogger().severe("Debug: " + playerData.toString());
                }
            }
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().warning(e.getMessage());
        }
        return playerData;
    }
}
