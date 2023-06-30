/**
 * Provides classes for managing player data.
 * This package includes classes for storing and retrieving player information,
 * by utilizing MySQL, Redis, and Caffeine.
 * <p>
 *     The main classes in this package are:
 *     <ul>
 *         <li>{@link com.inferris.player.PlayerData}: Represents the data and attributes associated with a player.
 *         <li>{@link com.inferris.player.PlayerDataManager}: Manages the data layers associated with a player, with storing, retrieving, and general management.
 * </p>
 * <p>
 *     The {@code PlayerData} class encapsulates the registry, rank, and coins information of a player.
 *     It provides methods to access and modify the player's registry, rank, and coins.
 *     The class also provides convenience methods to work with player ranks and their branches.
 * </p>
 * <p>
 *     Example usage:
 *     <pre>{@code
 *     PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
 *     Rank rank = playerData.getRank();
 *     Registry registry = playerData.getRegistry();
 *     Coins coins = playerData.getCoins();
 *     }</pre>
 * </p>
 *
 * @since 1.0
 *
 * @see com.inferris.player.PlayerData
 * @see com.inferris.player.PlayerDataManager
 * @see com.inferris.player.registry.Registry
 * @see com.inferris.player.registry.RegistryManager
 * @see com.inferris.rank.Rank
 * @see com.inferris.rank.RanksManager
 * @see com.inferris.rank.Branch
 * @see com.inferris.player.coins.Coins
 */
package com.inferris.player;