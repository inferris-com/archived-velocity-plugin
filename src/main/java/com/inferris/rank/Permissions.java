package com.inferris.rank;

import com.inferris.Inferris;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Permissions {

    public static void attachPermissions(ProxiedPlayer player) {
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
        List<RankRegistry> ranks = playerData.getByBranches();

        Configuration permissionsConfig = Inferris.getPermissionsConfiguration();
        Configuration ranksSection = permissionsConfig.getSection("ranks");

        if (ranksSection != null) {

            Collection<String> rankNames = ranksSection.getKeys();
            List<String> nonePermissions = ranksSection.getStringList("none");

            for (String rankName : rankNames) {
                Inferris.getInstance().getLogger().warning(rankName);

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
    }



//        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
//        RankRegistry rank = playerData.getByBranch();
//        for(Permission permission : Permission.values()){
//            for(RankRegistry permissionRank : permission.getRanks()){
//                if(permissionRank == rank){
//                    player.setPermission(permission.getPermission(), true);
//                }
//            }
        }
