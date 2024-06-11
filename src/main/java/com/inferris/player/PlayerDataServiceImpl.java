package com.inferris.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.inferris.Inferris;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RankRegistry;
import com.inferris.rank.RanksManager;
import com.inferris.server.Server;
import com.inferris.server.ServerState;
import com.inferris.util.SerializationUtils;
import com.inferris.util.ServerUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Implementation of the PlayerDataService interface for accessing and updating player data.
 * <p>
 * This class provides concrete implementations of the methods defined in the PlayerDataService interface.
 * It also interacts with the {@link com.inferris.player.PlayerDataManager} to perform data retrieval and updates, ensuring that changes are
 * properly persisted and propagated as needed.
 * <p>
 *     Example usage:
 *     <pre>{@code
 *     PlayerDataService playerDataService = new PlayerDataServiceImpl(playerDataManager);
 *     }</pre>
 * @see com.inferris.player.PlayerDataService
 * @see com.inferris.Inferris
 */
public class PlayerDataServiceImpl implements PlayerDataService {
    private final PlayerDataManager playerDataManager;
    private final FetchPlayer fetchPlayer;

    public PlayerDataServiceImpl(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
        this.fetchPlayer = new FetchPlayer(this);
    }
    @Override
    public void getPlayerData(UUID uuid, Consumer<PlayerData> operation) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        operation.accept(playerData);
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        return PlayerDataManager.getInstance().getPlayerData(uuid);
    }

    @Override
    public CompletableFuture<PlayerData> getPlayerDataAsync(UUID uuid) {
        return playerDataManager.getPlayerDataAsync(uuid);
    }
    @Override
    public void updatePlayerData(UUID uuid, Consumer<PlayerData> updateFunction) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        updateFunction.accept(playerData);
        playerDataManager.updatePlayerDataTable(playerData);
        playerDataManager.updateAllDataAndPush(uuid, playerData);
    }

    @Override
    public void updatePlayerDataWithoutPush(UUID uuid, Consumer<PlayerData> updateFunction) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        updateFunction.accept(playerData);
        playerDataManager.updateAllData(uuid, playerData);
    }

    @Override
    public void updateProfileField(UUID uuid, Consumer<Profile> updateFunction) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        updateFunction.accept(playerData.getProfile());
        playerDataManager.updateProfileTable(playerData.getProfile(), playerData.getUuid());
        playerDataManager.updateAllDataAndPush(uuid, playerData);
    }

    @Override
    public void updateRankField(UUID uuid, Consumer<Rank> updateFunction) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        updateFunction.accept(playerData.getRank());
        playerDataManager.updateRankTable(playerData.getRank(), playerData.getUuid());
        playerDataManager.updateAllDataAndPush(uuid, playerData);
    }

    @Override
    public UUID fetchUUIDByUsername(String username) {
        return fetchPlayer.getUUIDByUsername(username);
    }

    @Override
    public boolean hasUUIDByUsername(String username) {
        return fetchPlayer.hasUUIDByUsername(username);
    }

    @Override
    public PlayerData fetchPlayerDataFromDatabase(UUID uuid) {
        return fetchPlayer.getPlayerDataFromDatabase(uuid);
    }

    @Override
    public PlayerData fetchPlayerDataFromDatabase(UUID uuid, String username, boolean insertData) {
        return fetchPlayer.getPlayerDataFromDatabase(uuid, username, insertData);
    }

    /**
     * Checks if the provided player has joined before by looking up their data in Redis and caching within Caffeine if necessary.
     *
     * @param player The ProxiedPlayer object representing the player to check.
     */
    @Override
    public boolean hasJoinedBefore(UUID uuid) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        ServerUtil.log("Checking Jedis #checkJoinedBefore", Level.WARNING, ServerState.DEBUG);
        String playerUUIDString = uuid.toString();
        PlayerDataService playerDataService = ServiceLocator.getPlayerDataService();

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
                boolean wasInserted = playerDataManager.insertPlayerDataIfNotExists(uuid, player.getName());
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