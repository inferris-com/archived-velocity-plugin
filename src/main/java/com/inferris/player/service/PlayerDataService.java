package com.inferris.player.service;

import com.inferris.player.Profile;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.PlayerData;
import com.inferris.rank.Rank;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Service interface for accessing and updating player data.
 * <p>
 * This interface defines the contract for retrieving and manipulating player data. It provides methods
 * for synchronous and asynchronous retrieval of player data, as well as methods for updating various fields
 * of the player data such as profile and rank.
 * <p>
 * Implementations of this interface are responsible for interacting with the underlying data storage amd
 * management system to perform these operations.
 * Example usage:
 * <pre>{@code PlayerDataService dataService = ServiceLocator.getPlayerDataService();}
 *     </pre>
 *
 * @see PlayerDataServiceImpl
 * @see PlayerContext
 */
public interface PlayerDataService {
    void setPlayerDataRepository(PlayerDataRepository playerDataRepository);
    PlayerDataRepository getPlayerDataRepository();
    PlayerData getPlayerData(UUID uuid);

    void getPlayerData(UUID uuid, Consumer<PlayerData> operation);

    CompletableFuture<PlayerData> getPlayerDataAsync(UUID uuid);

    void updatePlayerData(UUID uuid, Consumer<PlayerData> updateFunction);

    void updateDatabase(UUID uuid, Consumer<PlayerData> updateFunction);

    void updateLocalPlayerData(UUID uuid, Consumer<PlayerData> updateFunction);

    void updateProfileField(UUID uuid, Consumer<Profile> updateFunction);

    void updateRankField(UUID uuid, Consumer<Rank> updateFunction);

    boolean hasJoinedBefore(UUID uuid);

    boolean hasUUIDByUsername(String username);

    UUID fetchUUIDByUsername(String username);

    PlayerData fetchPlayerDataFromDatabase(UUID uuid);

    PlayerData fetchPlayerDataFromDatabase(UUID uuid, String username, boolean insertData);

    void nukePlayerData(UUID uuid);
}