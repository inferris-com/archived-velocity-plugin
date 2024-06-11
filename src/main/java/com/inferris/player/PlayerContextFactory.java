package com.inferris.player;

import java.util.UUID;

/**
 * Factory class for creating PlayerContext instances.
 * <p>
 * This factory provides a method to create a new {@link com.inferris.player.PlayerContext} for a given player UUID
 * Using a factory ensures that PlayerContext instances are created in a consistent and controlled manner.
 * <p>
 * The factory encapsulates the construction logic of PlayerContext, making it easier to manage dependencies
 * and instantiation details.
 *
 * @see com.inferris.player.PlayerContext
 * @see com.inferris.player.PlayerDataService
 */
public class PlayerContextFactory {
    public static PlayerContext create(UUID uuid, PlayerDataService playerDataService) {
        return new PlayerContext(uuid, playerDataService);
    }
}