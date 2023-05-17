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

    public static void printYamlFile(String filePath) {
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);

            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(fis);

            if (data != null) {
                Inferris.getInstance().getLogger().severe(data + "");
            } else {
                Inferris.getInstance().getLogger().severe("YAML file is empty or couldn't be loaded.");
            }
        } catch (FileNotFoundException e) {
            Inferris.getInstance().getLogger().severe("YAML file not found: " + e.getMessage());
        }
    }

    public static void attachPermissions(ProxiedPlayer player) {
        Inferris.getInstance().getLogger().severe("attaching permissions");
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
        List<RankRegistry> ranks = playerData.getByBranches();
        Inferris.getInstance().getLogger().severe("attaching permissions " + player.getUniqueId());
        printYamlFile(Inferris.getPermissionsFile().getPath());


        Configuration permissionsConfig = Inferris.getPermissionsConfiguration();
        Configuration ranksSection = permissionsConfig.getSection("ranks");
        if(Inferris.getPermissionsConfiguration() != null) {
            Inferris.getInstance().getLogger().severe("permConfig not null");

        }

        if (ranksSection != null) {
            Inferris.getInstance().getLogger().severe("Not null");

            Collection<String> rankNames = ranksSection.getKeys();
            List<String> nonePermissions = ranksSection.getStringList("none");
            Inferris.getInstance().getLogger().severe("Ranks : " + ranks + "");
            Inferris.getInstance().getLogger().severe("Ranknames : " + rankNames + "");
            Inferris.getInstance().getLogger().severe("ranksSection: " + ranksSection.getKeys());

            for (String rankName : rankNames) {
                Inferris.getInstance().getLogger().warning(rankName);

                if (ranks.contains(RankRegistry.valueOf(rankName.toUpperCase()))) {
                    Inferris.getInstance().getLogger().severe("If rank contains rankregistry: " + rankName);
                    List<String> permissions = ranksSection.getStringList(rankName);
                    Inferris.getInstance().getLogger().severe("Permissions " + permissions);

                    for (String permission : permissions) {
                        player.setPermission(permission, true);
                        Inferris.getInstance().getLogger().severe("Setting permission : " + permission);

                    }
                }
            }
            for (String nonePermission : nonePermissions) {
                player.setPermission(nonePermission, true);
                Inferris.getInstance().getLogger().severe("Setting permission : " + nonePermission);

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
