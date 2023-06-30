/**
 * Provides classes and utilities for managing player registries and data.
 * The player registry package includes functionality to add, retrieve, update, and delete player registries.
 * It also contains related classes for managing player channels and vanish states.
 * This package interacts with the Redis server and the player database.
 *
 * <p>Classes included in this package:</p>
 * <ul>
 *   <li>{@link com.inferris.player.registry.RegistryManager} - Manages player registries and interacts with Redis and the player database.</li>
 *   <li>{@link com.inferris.player.registry.Registry} - Represents a player registry, storing UUID, username, channel, and vanish state information.</li>
 * </ul>
 *
 * @see com.inferris.player.registry.Registry
 * @see com.inferris.player.registry.RegistryManager
 * @see com.inferris.player.PlayerData
 * @see com.inferris.player.PlayerDataManager
 * <p>
 *     Example usage:
 *     <pre>{@code
 *     Registry registry = PlayerDataManager.getInstance().getPlayerData(player).getRegistry();
 *     registry.setVanishState(VanishState.ENABLED);
 *     }</pre>
 * </p>
 * @since 1.0.0
 */
package com.inferris.player.registry;
