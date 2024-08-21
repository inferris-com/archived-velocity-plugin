package com.inferris.player.service;

import com.google.inject.Inject;
import com.inferris.Inferris;
import com.inferris.events.redis.EventPayload;
import com.inferris.events.redis.PlayerAction;
import com.inferris.player.Profile;
import com.inferris.player.PlayerData;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.server.jedis.JedisChannel;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Implementation of the PlayerDataService interface for accessing and updating player data.
 * <p>
 * This class provides concrete implementations of the methods defined in the PlayerDataService interface.
 * It also interacts with the {@link PlayerDataManager} to perform data retrieval and updates, ensuring that changes are
 * properly persisted and propagated as needed.
 * <p>
 * Example usage:
 * <pre>{@code
 *     PlayerDataService playerDataService = new PlayerDataServiceImpl(playerDataManager);
 *     }</pre>
 *
 * @see PlayerDataService
 * @see Inferris
 */
public class PlayerDataServiceImpl implements PlayerDataService {
    private final PlayerDataManager playerDataManager;
    private final PlayerDataRepository playerDataRepository;
    private final ManagerContainer managerContainer;

    @Inject
    public PlayerDataServiceImpl(PlayerDataManager playerDataManager, PlayerDataRepository playerDataRepository, ManagerContainer managerContainer) { //todo remove pd manager
        this.playerDataManager = playerDataManager;
        this.playerDataRepository = playerDataRepository;
        this.managerContainer = managerContainer;
    }

    @Override
    public void setPlayerDataRepository(PlayerDataRepository playerDataRepository) {

    }

    @Override
    public PlayerDataRepository getPlayerDataRepository() {
        return playerDataRepository;
    }

    @Override
    public void getPlayerData(UUID uuid, Consumer<PlayerData> operation) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        operation.accept(playerData);
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataManager.getPlayerData(uuid);
    }

    @Override
    public CompletableFuture<PlayerData> getPlayerDataAsync(UUID uuid) {
        return playerDataManager.getPlayerDataAsync(uuid);
    }

    @Override
    public void invalidate(UUID uuid) {
        playerDataManager.invalidateCache(uuid);
    }

    @Override
    public boolean hasAccess(UUID uuid) {
        return playerDataRepository.hasAccess(uuid);
    }

    @Override
    public void updatePlayerData(UUID uuid, Consumer<PlayerData> updateFunction) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        updateFunction.accept(playerData);
        playerDataManager.updateAllDataAndPush(uuid, playerData);
    }

    @Override
    public void updateDatabase(UUID uuid, Consumer<PlayerData> updateFunction) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        updateFunction.accept(playerData);
        playerDataRepository.updatePlayerDataTableAsync(playerData);
    }

    @Override
    public void updateLocalPlayerData(UUID uuid, Consumer<PlayerData> updateFunction) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        updateFunction.accept(playerData);
        playerDataManager.updateAllData(uuid, playerData);
    }

    @Override
    public void updateProfileField(UUID uuid, Consumer<Profile> updateFunction) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        updateFunction.accept(playerData.getProfile());
        playerDataRepository.updateProfileTable(playerData.getProfile(), playerData.getUuid());
        playerDataManager.updateAllDataAndPush(uuid, playerData);
    }

    @Override
    public void updateRankField(UUID uuid, Consumer<Rank> updateFunction) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        updateFunction.accept(playerData.getRank());
        playerDataRepository.updateRankTable(playerData.getRank(), playerData.getUuid());
        playerDataManager.updateAllDataAndPush(uuid, playerData);
    }

    @Override
    public void updateCoins(UUID uuid, int amount) {
        playerDataRepository.updateCoins(uuid, amount);
        updatePlayerData(uuid, pd ->
                pd.setCoins(amount));
    }

    @Override
    public void setRank(UUID uuid, Branch branch, int level) {
        managerContainer.getRanksManager().setRank(uuid, branch, level);
    }

    @Override
    public void setRank(UUID uuid, Branch branch, int level, boolean hasMessage) {
        managerContainer.getRanksManager().setRank(uuid, branch, level);

    }

    @Override
    public void setVanished(UUID uuid, boolean isEnabled) {
        Inferris.getInstance().getLogger().info("Setting vanish state for player: " + uuid + " to " + isEnabled);

        updatePlayerData(uuid, playerData1 -> {
            playerData1.setVanishState(isEnabled ? VanishState.ENABLED : VanishState.DISABLED);

            try (Jedis jedis = Inferris.getJedisPool().getResource()) {
                jedis.publish(JedisChannel.PLAYERDATA_VANISH.getChannelName(), new EventPayload(uuid,
                        PlayerAction.UPDATE_PLAYER_DATA,
                        null,
                        Inferris.getInstanceId()).toPayloadString());
                Inferris.getInstance().getLogger().info("Completed vanish state update for player: " + uuid);

                playerDataRepository.updatePlayerDataTable(getPlayerData(uuid));
            }
        });
    }

    @Override
    public UUID fetchUUIDByUsername(String username) {
        FetchPlayer fetchPlayer = new FetchPlayer(playerDataRepository);
        return fetchPlayer.getUUIDByUsername(username);
    }

    @Override
    public boolean hasUUIDByUsername(String username) {
        FetchPlayer fetchPlayer = new FetchPlayer(playerDataRepository);
        return fetchPlayer.hasUUIDByUsername(username);
    }

    @Override
    public PlayerData fetchPlayerDataFromDatabase(UUID uuid) {
        return playerDataRepository.getPlayerDataFromDatabase(uuid);
    }

    @Override
    public void nukePlayerData(UUID uuid) {
        playerDataRepository.deletePlayerData(uuid);
    }

    @Override
    public PlayerData fetchPlayerDataFromDatabase(UUID uuid, String username, boolean insertData) {
        return playerDataRepository.getPlayerDataFromDatabase(uuid, username, insertData);
    }

    /**
     * Checks if the provided player has joined before by looking up their data in Redis and caching within Caffeine if necessary.
     *
     * @param uuid The ProxiedPlayer object representing the player to check.
     */
    @Override
    public boolean hasJoinedBefore(UUID uuid) {
        return playerDataRepository.hasJoinedBefore(uuid);
    }
}