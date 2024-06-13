package com.inferris.player.service;

import com.inferris.player.Profile;
import com.inferris.player.PlayerData;
import com.inferris.rank.Rank;

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
 *     Example usage:
 *     <pre>{@code
 *     PlayerDataService playerDataService = new PlayerDataServiceImpl(playerDataManager);
 *     }</pre>
 * @see PlayerDataService
 * @see com.inferris.Inferris
 */
public class PlayerDataServiceImpl implements PlayerDataService {
    private final PlayerDataManager playerDataManager;
    private PlayerDataRepository playerDataRepository;

    public PlayerDataServiceImpl(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public void setPlayerDataRepository(PlayerDataRepository playerDataRepository) {
        this.playerDataRepository = playerDataRepository;
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
        playerDataManager.updateAllDataAndPush(uuid, playerData);
    }

    @Override
    public void updateDatabase(UUID uuid, Consumer<PlayerData> updateFunction) {
        PlayerData playerData = playerDataManager.getPlayerData(uuid);
        updateFunction.accept(playerData);
        playerDataRepository.updatePlayerDataTable(playerData);
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
    public UUID fetchUUIDByUsername(String username) {
        FetchPlayer fetchPlayer = new FetchPlayer();
        return fetchPlayer.getUUIDByUsername(username);
    }

    @Override
    public boolean hasUUIDByUsername(String username) {
        FetchPlayer fetchPlayer = new FetchPlayer();
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