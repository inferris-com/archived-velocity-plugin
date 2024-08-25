/*
 * Copyright (c) 2024. Inferris.
 * All rights reserved.
 */

package com.inferris.player.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.database.Table;
import com.inferris.player.*;
import com.inferris.player.channel.Channel;
import com.inferris.player.friends.Friends;
import com.inferris.player.friends.FriendsManager;
import com.inferris.player.manager.ManagerContainer;
import com.inferris.player.manager.PlayerDataManager;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Rank;
import com.inferris.server.Server;
import com.inferris.server.ServerState;
import com.inferris.server.jedis.JedisHelper;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class PlayerDataRepository {

    /**
     * Database operations
     */

    private final PlayerDataService playerDataService;
    private final ManagerContainer managerContainer;
    private final ExecutorService executorService;

    @Inject
    public PlayerDataRepository(PlayerDataService playerDataService, ManagerContainer managerContainer, ExecutorService executorService) {
        this.playerDataService = playerDataService;
        this.managerContainer = managerContainer;
        this.executorService = executorService != null ? executorService : Executors.newCachedThreadPool();
    }

    public CompletableFuture<Void> updatePlayerDataTableAsync(PlayerData playerData) {
        return CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();

            String[] columnNames = {"username", "coins", "channel", "vanished"};
            boolean isVanished = playerData.getVanishState() == VanishState.ENABLED;
            Object[] values = {playerData.getUsername(), playerData.getCoins(), playerData.getChannel().name(), isVanished};
            String whereClause = "uuid = ?";
            try {
                DatabaseUtils.updateData("player_data", columnNames, values, whereClause, playerData.getUuid());
            } catch (SQLException e) {
                Inferris.getInstance().getLogger().warning("Failed to update player_data in the database: " + e.getMessage());
            } finally {
                long duration = System.currentTimeMillis() - startTime;
            }
        }, executorService);
    }

    public void updatePlayerDataTable(PlayerData playerData) {
        updatePlayerDataTableAsync(playerData).handle((result, ex) -> {
            if (ex != null) {
                Inferris.getInstance().getLogger().warning("Failed to update player_data: " + ex.getMessage());
            } else {
            }
            return null;
        });
    }

    public CompletableFuture<Void> updateRankTable(Rank rank, UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            String[] columnNames = {"staff", "builder", "donor", "other"};
            Object[] values = {rank.getStaff(), rank.getBuilder(), rank.getDonor(), rank.getOther()};
            String whereClause = "uuid = ?";
            try {
                DatabaseUtils.updateData("rank", columnNames, values, whereClause);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update rank in the database", e);
            }
        }, executorService);
    }

    public CompletableFuture<Void> updateProfileTable(Profile profile, UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            String[] columnNames = {"join_date", "bio", "pronouns", "xenforo_id", "discord_linked", "is_flagged"};
            Object[] values = {profile.getRegistrationDate(), profile.getBio(), profile.getPronouns(), profile.getXenforoId(), profile.isDiscordLinked(), profile.isFlagged()};
            String whereClause = "uuid = ?";
            try {
                DatabaseUtils.updateData("profile", columnNames, values, whereClause);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update profile in the database", e);
            }
        }, executorService);
    }

    public void updateCoins(UUID uuid, int amount) {
        try {
            updateCoinsAsync(uuid, amount).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> updateCoinsAsync(UUID uuid, int amount) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = DatabasePool.getConnection();
                 PreparedStatement updateStatement = connection.prepareStatement("UPDATE player_data SET coins = ? WHERE uuid = ?")) {
                updateStatement.setInt(1, amount);
                updateStatement.setString(2, uuid.toString());
                updateStatement.executeUpdate();
            } catch (SQLException e) {
                Inferris.getInstance().getLogger().severe("SQLException from CoinsManager: " + e.getMessage());
            }
        }, executorService);
    }

    public PlayerData getPlayerDataFromDatabase(UUID uuid) {
        try {
            return getPlayerDataFromDatabaseAsync(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public PlayerData getPlayerDataFromDatabase(UUID uuid, String initialUsername, boolean insertData) {
        try {
            return getPlayerDataFromDatabaseAsync(uuid, initialUsername, insertData).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasJoinedBefore(UUID uuid) {
        try {
            return hasJoinedBeforeAsync(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasAccess(UUID uuid) {
        try {
            return hasAccessAsync(uuid).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // Asynchronous method to get player data from database
    public CompletableFuture<PlayerData> getPlayerDataFromDatabaseAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerData playerData = null;
            try (Connection connection = DatabasePool.getConnection();
                 PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM " + Table.PLAYER_DATA.getName() + " WHERE uuid = ?")) {

                queryStatement.setString(1, uuid.toString());
                ResultSet resultSet = queryStatement.executeQuery();

                if (resultSet.next()) {
                    playerData = mapResultSetToPlayerData(resultSet, uuid, connection);
                }
            } catch (SQLException e) {
                Inferris.getInstance().getLogger().warning(e.getMessage());
            }
            return playerData;
        }, executorService);
    }

    // Asynchronous method to get or insert player data from/to the database
    public CompletableFuture<PlayerData> getPlayerDataFromDatabaseAsync(UUID uuid, String initialUsername, boolean insertData) {
        return CompletableFuture.supplyAsync(() -> {
            PlayerData playerData = null;
            try (Connection connection = DatabasePool.getConnection();
                 PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM " + Table.PLAYER_DATA.getName() + " WHERE uuid = ?")) {

                queryStatement.setString(1, uuid.toString());
                ResultSet resultSet = queryStatement.executeQuery();

                if (resultSet.next()) {
                    playerData = mapResultSetToPlayerData(resultSet, uuid, connection);
                } else if (insertData) {
                    insertPlayerDataToDatabase(connection, uuid, initialUsername).join();
                    playerData = playerDataService.getPlayerData(uuid);
                }
            } catch (SQLException e) {
                Inferris.getInstance().getLogger().warning(e.getMessage());
            }
            return playerData;
        }, executorService);
    }

    // Helper method to map ResultSet to PlayerData
    private PlayerData mapResultSetToPlayerData(ResultSet resultSet, UUID uuid, Connection connection) throws SQLException {
        String username = resultSet.getString("username");
        int coins = resultSet.getInt("coins");
        Channel channel = Channel.valueOf(resultSet.getString("channel"));
        int vanished = resultSet.getInt("vanished");

        VanishState vanishState = (vanished == 1) ? VanishState.ENABLED : VanishState.DISABLED;
        Rank rank = managerContainer.getRanksManager().loadRanks(uuid, connection);

        if (ProxyServer.getInstance().getPlayer(uuid) != null) {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

            // Username change
            if (!username.equals(player.getName())) {
                username = player.getName();
                playerDataService.updateLocalPlayerData(player.getUniqueId(), pdToUpdate -> pdToUpdate.setUsername(player.getName()));
            }
        }

        Profile profile = loadProfile(uuid, connection);
        return new PlayerData(uuid, username, rank, profile, coins, channel, vanishState, Server.LOBBY);
    }

    // Helper method to load Profile from the database. Used if they already exist, otherwise will insert.
    private Profile loadProfile(UUID uuid, Connection connection) throws SQLException {
        Profile profile = null;
        try (PreparedStatement selectProfileStatement = connection.prepareStatement("SELECT * FROM " + Table.PROFILE.getName() + " WHERE uuid = ?")) {
            selectProfileStatement.setString(1, uuid.toString());
            ResultSet profileResultSet = selectProfileStatement.executeQuery();

            if (profileResultSet.next()) {
                long registrationDate = profileResultSet.getLong("join_date");
                String bio = profileResultSet.getString("bio");
                String pronouns = profileResultSet.getString("pronouns");
                int xenforoId = profileResultSet.getInt("xenforo_id");
                int discordLinked = profileResultSet.getInt("discord_linked");
                boolean isDiscordLinked = (discordLinked != 0);
                // Check if the player is flagged in the flagged_players table
                boolean isFlagged = false;
                try (PreparedStatement flaggedCheckStatement = connection.prepareStatement("SELECT 1 FROM " + Table.FLAGGED_PLAYERS.getName() + " WHERE uuid = ?")) {
                    flaggedCheckStatement.setString(1, uuid.toString());
                    ResultSet flaggedResultSet = flaggedCheckStatement.executeQuery();
                    isFlagged = flaggedResultSet.next();
                }

                profile = new Profile(registrationDate, bio, pronouns, xenforoId, isDiscordLinked, isFlagged);
            }
        }
        return profile;
    }

    public CompletableFuture<Boolean> insertPlayerDataIfNotExists(UUID uuid, String username) {
        Inferris.getInstance().getLogger().warning("Attempting to call: insertPlayerDataIfNotExists");
        return CompletableFuture.supplyAsync(() -> {
            boolean inserted = false;
            try (Connection connection = DatabasePool.getConnection()) {
                // Check if the player data exists in the database
                PreparedStatement queryStatement = connection.prepareStatement("SELECT COUNT(*) FROM " + Table.PLAYER_DATA.getName() + " WHERE uuid = ?");
                queryStatement.setString(1, uuid.toString());
                ResultSet resultSet = queryStatement.executeQuery();

                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    // Insert the player data if it doesn't exist
                    Inferris.getInstance().getLogger().warning("Attempting to insert (211)");
                    insertPlayerDataToDatabase(connection, uuid, username).join();
                    inserted = true;
                }
            } catch (SQLException e) {
                Inferris.getInstance().getLogger().warning(e.getMessage());
            }

            return inserted;
        }, executorService).thenCompose(inserted -> {
            // Retrieve the player data asynchronously. Returns Player Data object that we need
            return getPlayerDataFromDatabaseAsync(uuid, username, false)
                    .thenApply(playerData -> {
                        if (playerData != null) {
                            try (Jedis jedis = Inferris.getJedisPool().getResource()) {
                                // Cache the player data and update Redis
                                String playerDataJson = SerializationUtils.serializePlayerData(playerData);
                                if (playerDataJson != null) {
                                    jedis.hset("playerdata", uuid.toString(), playerDataJson);
                                    managerContainer.getPlayerDataManager().updateCaffeineCache(uuid, playerData);
                                } else {
                                    Inferris.getInstance().getLogger().warning("Serialized player data is null for UUID: " + uuid);
                                }
                            } catch (JsonProcessingException e) {
                                Inferris.getInstance().getLogger().warning(e.getMessage());
                            }
                        } else {
                            Inferris.getInstance().getLogger().warning("Player data is null for UUID: " + uuid);
                            Inferris.getInstance().getLogger().info("Attempting to remove Redis entry");
                            JedisHelper.hdel("playerdata", uuid.toString());
                        }
                        return inserted;
                    });
        });
    }

    public CompletableFuture<Void> insertPlayerDataToDatabase(Connection connection, UUID uuid, String username) {
        return CompletableFuture.runAsync(() -> {
            Inferris.getInstance().getLogger().warning("#insertPlayerDataToDatabase, 234, works good");
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
            } catch (SQLException e) {
                Inferris.getInstance().getLogger().warning(e.getMessage());
            }
        }, executorService);
    }

    public void deletePlayerData(UUID uuid) {
        PlayerData playerData = playerDataService.getPlayerData(uuid);
        FriendsManager friendsManager = managerContainer.getFriendsManager();
        // Step 1: Fetch the list of friends
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

    public CompletableFuture<Boolean> hasJoinedBeforeAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
            PlayerDataManager playerDataManager = managerContainer.getPlayerDataManager();
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
                    boolean wasInserted = insertPlayerDataIfNotExists(uuid, player.getName()).join();
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
        }, executorService);
    }

    public CompletableFuture<Boolean> hasAccessAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            ServerUtil.log("Checking access restriction", Level.WARNING, ServerState.DEBUG);

            try (Connection connection = DatabasePool.getConnection()) {
                String[] column = {"uuid"};
                String condition = "`uuid` = ?";
                ResultSet resultSet = DatabaseUtils.executeQuery(connection, "controlled_access", column, condition, uuid.toString());
                if (resultSet.next()) {
                    return true;
                }
            } catch (SQLException e) {
                Inferris.getInstance().getLogger().severe("Error: " + e.getMessage());
            }
            return false;
        }, executorService);
    }
}