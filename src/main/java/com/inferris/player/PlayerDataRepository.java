package com.inferris.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.database.Table;
import com.inferris.player.friends.Friends;
import com.inferris.player.friends.FriendsManager;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Rank;
import com.inferris.rank.RanksManager;
import com.inferris.server.Server;
import com.inferris.server.ServerState;
import com.inferris.util.DatabaseUtils;
import com.inferris.util.SerializationUtils;
import com.inferris.util.ServerUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerDataRepository {
    private final PlayerDataService playerDataService = ServiceLocator.getPlayerDataService();
    private final PlayerDataManager playerDataManager = PlayerDataManager.getInstance();

    public void updatePlayerDataTable(PlayerData playerData) {
        String[] columnNames = {"username", "coins", "channel", "vanished"};
        boolean isVanished = playerData.getVanishState() == VanishState.ENABLED;
        Object[] values = {playerData.getUsername(), playerData.getCoins(), playerData.getChannel().name(), isVanished};
        String whereClause = "uuid = ?";
        try (Connection connection = DatabasePool.getConnection()) {
            DatabaseUtils.updateData(connection, "player_data", columnNames, values, whereClause);
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().warning("Failed to update player_data in the database: " + e.getMessage());
        }
    }

    public void updateRankTable(Rank rank, UUID uuid) {
        String[] columnNames = {"staff", "builder", "donor", "other"};
        Object[] values = {rank.getStaff(), rank.getBuilder(), rank.getDonor(), rank.getOther()};
        String whereClause = "uuid = ?";
        try (Connection connection = DatabasePool.getConnection()) {
            DatabaseUtils.updateData(connection, "rank", columnNames, values, whereClause);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update rank in the database", e);
        }
    }

    public void updateProfileTable(Profile profile, UUID uuid) {
        String[] columnNames = {"join_date", "bio", "pronouns", "xenforo_id", "discord_linked", "is_flagged"};
        Object[] values = {profile.getRegistrationDate(), profile.getBio(), profile.getPronouns(), profile.getXenforoId(), profile.isDiscordLinked(), profile.isFlagged()};
        String whereClause = "uuid = ?";
        try (Connection connection = DatabasePool.getConnection()) {
            DatabaseUtils.updateData(connection, "profile", columnNames, values, whereClause);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update profile in the database", e);
        }
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
                    insertPlayerDataToDatabase(connection, uuid, username);
                    playerData = playerDataService.getPlayerData(uuid);
                    Inferris.getInstance().getLogger().severe("Debug: " + playerData.toString());
                }
            }
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().warning(e.getMessage());
        }
        return playerData;
    }

    /**
     * Inserts the player data into the database if it does not already exist.
     *
     * @param uuid     The UUID of the player.
     * @param username The username of the player.
     * @return true if the player data was inserted, false if it already exists.
     */
    public boolean insertPlayerDataIfNotExists(UUID uuid, String username) {
        boolean inserted = false;
        try (Connection connection = DatabasePool.getConnection()) {
            // Check if the player data exists in the database
            PreparedStatement queryStatement = connection.prepareStatement("SELECT COUNT(*) FROM " + Table.PLAYER_DATA.getName() + " WHERE uuid = ?");
            queryStatement.setString(1, uuid.toString());
            ResultSet resultSet = queryStatement.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) == 0) {
                // Insert the player data if it doesn't exist
                insertPlayerDataToDatabase(connection, uuid, username);
                inserted = true;
            }

            // Retrieve the player data
            PlayerData playerData = getPlayerDataFromDatabase(uuid, username, false);

            // Cache the player data and update Redis
            String playerDataJson = SerializationUtils.serializePlayerData(playerData);
            try (Jedis jedis = Inferris.getJedisPool().getResource()) {
                jedis.hset("playerdata", uuid.toString(), playerDataJson);
                PlayerDataManager.getInstance().updateCaffeineCache(uuid, playerData);
            }
        } catch (SQLException | JsonProcessingException e) {
            Inferris.getInstance().getLogger().warning(e.getMessage());
        }
        return inserted;
    }

    // Insert a new player into the database - default values
    public void insertPlayerDataToDatabase(Connection connection, UUID uuid, String username) {
        try (PreparedStatement insertPlayersStatement = connection.prepareStatement("INSERT INTO " + Table.PLAYER_DATA.getName() + " (uuid, username, coins, channel, vanished) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement insertProfileStatement = connection.prepareStatement("INSERT INTO " + Table.PROFILE.getName() + " (uuid, join_date, bio, pronouns) VALUES (?, ?, ?, ?)")) {

            insertPlayersStatement.setString(1, uuid.toString());
            insertPlayersStatement.setString(2, username);
            insertPlayersStatement.setInt(3, PlayerDefault.COIN_BALANCE.getValue());
            insertPlayersStatement.setString(4, String.valueOf(Channel.NONE));
            insertPlayersStatement.setInt(5, 0);
            insertPlayersStatement.execute();

            insertProfileStatement.setString(1, uuid.toString());
            insertProfileStatement.setString(2, String.valueOf(Instant.now().getEpochSecond()));
            insertProfileStatement.setString(3, null);
            insertProfileStatement.setString(4, null);
            insertProfileStatement.execute();

            //RanksManager.getInstance().loadRanks(uuid, connection); todo remove
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().warning(e.getMessage());
        }
    }

    public void deletePlayerData(UUID uuid) {
        PlayerData playerData = playerDataService.getPlayerData(uuid);

        // Step 1: Fetch the list of friends
        FriendsManager friendsManager = FriendsManager.getInstance();
        Friends playerFriends = friendsManager.getFriendsData(uuid);
        List<UUID> friendsList = new ArrayList<>(playerFriends.getFriendsList());

        // Step 2: Update each friend's data
        for (UUID friendUUID : friendsList) {
            Friends friendData = friendsManager.getFriendsData(friendUUID);
            friendData.removeFriend(uuid);
            friendsManager.updateCache(friendUUID, friendData);
            friendsManager.updateRedisData(friendUUID, friendData);
        }

        // Step 3: Remove the player's data from all necessary tables
        try (Connection connection = DatabasePool.getConnection()) {
            DatabaseUtils.removeData(connection, Table.PLAYER_DATA.getName(), "`uuid` = '" + playerData.getUuid().toString() + "'");
            DatabaseUtils.removeData(connection, "`rank`", "`uuid` = '" + playerData.getUuid().toString() + "'");
            DatabaseUtils.removeData(connection, Table.PROFILE.getName(), "`uuid` = '" + playerData.getUuid().toString() + "'");
            DatabaseUtils.removeData(connection, Table.VERIFICATION.getName(), "`uuid` = '" + playerData.getUuid().toString() + "'");
            DatabaseUtils.removeData(connection, Table.VERIFICATION_SESSIONS.getName(), "`uuid` = '" + playerData.getUuid().toString() + "'");
        } catch (SQLException e) {
            Inferris.getInstance().getLogger().severe(e.getMessage());
        }

        // Step 4: Remove the player's data from Redis and cache
        try (Jedis jedis = Inferris.getJedisPool().getResource()) {
            jedis.hdel("playerdata", playerData.getUuid().toString());
            jedis.hdel("friends", playerData.getUuid().toString());
            friendsManager.getCaffeineCache().invalidate(uuid);
        }
    }

    public boolean hasJoinedBefore(UUID uuid) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        ServerUtil.log("Checking Jedis #checkJoinedBefore", Level.WARNING, ServerState.DEBUG);
        String playerUUIDString = uuid.toString();

        try (Jedis jedis = Inferris.getJedisPool().getResource()) {
            // Check if player data exists in Redis
            if (jedis.hexists("playerdata", playerUUIDString)) {
                ServerUtil.log("Exists in Jedis", Level.WARNING, ServerState.DEBUG);

                // Cache data in Caffeine if not present
                if (playerDataManager.getCache().getIfPresent(uuid) == null) {
                    String playerDataJson = jedis.hget("playerdata", playerUUIDString);
                    PlayerData playerData = SerializationUtils.deserializePlayerData(playerDataJson);
                    playerDataManager.updateCaffeineCache(uuid, playerData);
                    playerDataManager.logPlayerData(playerData);
                }
            } else {
                ServerUtil.log("Not in Redis, checking database", Level.WARNING, ServerState.DEBUG);

                // Check and insert player data into the database
                boolean wasInserted = insertPlayerDataIfNotExists(uuid, player.getName());
                if (wasInserted) {
                    ServerUtil.log("Inserted into database and Jedis", Level.WARNING, ServerState.DEBUG);
                    return false;
                }

                // Shouldn't reach here but just in case
            }
            return true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
