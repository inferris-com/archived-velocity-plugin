package com.inferris.player.context;

import com.inferris.player.*;
import com.inferris.player.channel.Channel;
import com.inferris.player.PlayerData;
import com.inferris.player.service.*;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.rank.RankRegistry;
import com.inferris.server.Server;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Provides a user-friendly interface for interacting with player data.
 * <p>
 * This class acts as a facade for accessing and modifying player-related information. It wraps the {@link PlayerData}
 * object and offers methods to interact with various aspects of the player's data, such as retrieving the player's
 * rank, profile, coins, and other attributes.
 * <p>
 * The PlayerContext is constructed with a UUID and a {@link PlayerDataService} instance, which it uses to fetch and
 * update player data. This design encapsulates the complexities of data access and manipulation, providing
 * a simpler API for client code.
 * <p>
 *     Example usage:
 *     <pre>{@code
 *     PlayerDataService dataService = ServiceLocator.getPlayerDataService();
 *     }</pre>
 *
 * @see PlayerDataService
 */
public class PlayerContext {
    private final UUID uuid;
    private final PlayerDataService playerDataService;
    private ManagerContainer managerContainer;
    private final PlayerData playerData;

    public PlayerContext(UUID uuid, PlayerDataService playerDataService) {
        this.uuid = uuid;
        this.playerDataService = playerDataService;
        this.playerData = playerDataService.getPlayerData(uuid);
    }

    public ProxiedPlayer getPlayer() {
        return ProxyServer.getInstance().getPlayer(uuid);
    }

    public boolean isStaff() {
        return playerData.getRank().getBranchID(Branch.STAFF) > 0;
    }

    public UUID getUuid() {
        return playerDataService.getPlayerData(uuid).getUuid();
    }
    public String getUsername() {
        return playerData.getUsername();
    }

    public Rank getRank() {
        return playerData.getRank();
    }

    public Profile getProfile() {
        return playerData.getProfile();
    }

    public int getCoins() {
        return playerData.getCoins();
    }

    public Channel getChannel() {
        return playerData.getChannel();
    }

    public VanishState getVanishState() {
        return playerData.getVanishState();
    }

    public Server getCurrentServer() {
        return playerData.getCurrentServer();
    }

    public ChatColor getNameColor() {
        RankRegistry highestRank = playerData.getRank().getByBranch();
        return highestRank != RankRegistry.NONE ? highestRank.getColor() : ChatColor.RESET;
    }

    public List<RankRegistry> getByBranches() {
        List<RankRegistry> ranks = new ArrayList<>();
        Rank rank = playerData.getRank();
        int staff = rank.getStaff();
        int builder = rank.getBuilder();
        int donor = rank.getDonor();

        if (staff == 3) {
            ranks.add(RankRegistry.ADMIN);
        } else if (staff == 2) {
            ranks.add(RankRegistry.MOD);
        } else if (staff == 1) {
            ranks.add(RankRegistry.HELPER);
        }
        if (builder == 1) {
            ranks.add(RankRegistry.BUILDER);
        }
        if (donor == 1) {
            ranks.add(RankRegistry.DONOR);
        }

        return ranks;
    }
}