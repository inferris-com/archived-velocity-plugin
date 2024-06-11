package com.inferris.rank;

import com.inferris.Inferris;
import com.inferris.config.ConfigType;
import com.inferris.player.*;
import com.inferris.server.ServerState;
import com.inferris.util.ServerUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class Permissions {
    private static final PlayerDataService playerDataService = ServiceLocator.getPlayerDataService();

    public static void attachPermissions(ProxiedPlayer player) {
        PlayerContext playerContext = PlayerContextFactory.create(player.getUniqueId(), playerDataService);
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player, "attachPermissions");
        List<RankRegistry> ranks = playerData.getRank().getApplicableRanks();

        Configuration permissionsConfig = Inferris.getInstance().getConfigurationHandler().getConfig(ConfigType.PERMISSIONS);
        Configuration ranksSection = permissionsConfig.getSection("ranks");

        if (ranksSection != null) {

            Collection<String> rankNames = ranksSection.getKeys();
            List<String> nonePermissions = ranksSection.getStringList("none");

            for (String rankName : rankNames) {
                ServerUtil.log(rankName, Level.INFO, ServerState.DEBUG, ServerState.DEV);


                if (ranks.contains(RankRegistry.valueOf(rankName.toUpperCase()))) {
                    List<String> permissions = ranksSection.getStringList(rankName);

                    for (String permission : permissions) {
                        player.setPermission(permission, true);
                    }
                }
            }
            for (String nonePermission : nonePermissions) {
                player.setPermission(nonePermission, true);
            }
        }

        RankRegistry rank = playerData.getRank().getByBranch();
        for(Permission permission : Permission.values()){
            for(RankRegistry permissionRank : permission.getRanks()){
                if(permissionRank == rank){
                    player.setPermission(permission.getPermission(), true);
                }
            }
        }
    }

    public static void listPermissions(ProxiedPlayer player) {
        PlayerContext playerContext = PlayerContextFactory.create(player.getUniqueId(), playerDataService);
        List<RankRegistry> ranks = playerContext.getRank().getApplicableRanks();
        Configuration permissionsConfig = Inferris.getInstance().getConfigurationHandler().getConfig(ConfigType.PERMISSIONS);
        Configuration ranksSection = permissionsConfig.getSection("ranks");

        if (ranksSection != null) {
            for (RankRegistry rank : ranks) {
                String rankName = rank.toString().toLowerCase();
                List<String> permissions = ranksSection.getStringList(rankName);

                if (!permissions.isEmpty()) {
                    player.sendMessage(new TextComponent(ChatColor.AQUA + "Permissions for " + rankName + ":"));
                    for (String permission : permissions) {
                        player.sendMessage(new TextComponent(permission));
                    }
                    // Add a new line to separate different ranks
                    player.sendMessage(new TextComponent(""));
                }
            }
        }
    }

}