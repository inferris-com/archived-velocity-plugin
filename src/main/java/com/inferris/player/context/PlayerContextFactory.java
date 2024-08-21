package com.inferris.player.context;

import com.inferris.player.service.PlayerDataService;

import java.util.UUID;

/**
 * Factory class for creating PlayerContext instances.
 * <p>
 * This factory provides a method to create a new {@link PlayerContext} for a given player UUID
 * Using a factory ensures that PlayerContext instances are created in a consistent and controlled manner.
 * Todo: use high level things here so won't always need service manager
 * <p>
 *
 *
 * @see PlayerContext
 * @see PlayerDataService
 */

@Deprecated
public class PlayerContextFactory {
    public static PlayerContext create(UUID uuid, PlayerDataService playerDataService) {
        return new PlayerContext(uuid, playerDataService);
    }
}