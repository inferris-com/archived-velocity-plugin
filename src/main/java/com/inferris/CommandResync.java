package com.inferris;

import com.inferris.database.DatabasePool;
import com.inferris.player.Channels;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.Profile;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Rank;
import com.inferris.util.DatabaseUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CommandResync extends Command {
    public CommandResync(String name) {
        super(name);
    }

    public CommandResync(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        if (sender instanceof ProxiedPlayer player) {
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
            String[] columnNames = {"staff", "builder", "donor", "other"};
            int staffRank = 0;
            int builderRank = 0;
            int donorRank = 0;
            int otherRank = 0;
            Connection connection = null;

            try {
                connection = DatabasePool.getConnection();
                ResultSet resultSet = DatabaseUtils.executeQuery(connection, "ranks", columnNames, "uuid = ?", player.getUniqueId());

                while (resultSet.next()) {
                    staffRank = resultSet.getInt("staff");
                    builderRank = resultSet.getInt("builder");
                    donorRank = resultSet.getInt("donor");
                    otherRank = resultSet.getInt("other");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Rank retrieveRankData(ProxiedPlayer player) {
        String[] columnNames = {"staff", "builder", "donor", "other"};
        int staffRank = 0;
        int builderRank = 0;
        int donorRank = 0;
        int otherRank = 0;

        try (Connection connection = DatabasePool.getConnection();
             ResultSet resultSet = DatabaseUtils.executeQuery(connection, "ranks", columnNames, "uuid = ?", player.getUniqueId())) {
            while (resultSet.next()) {
                staffRank = resultSet.getInt("staff");
                builderRank = resultSet.getInt("builder");
                donorRank = resultSet.getInt("donor");
                otherRank = resultSet.getInt("other");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Rank(staffRank, builderRank, donorRank, otherRank);
    }

    private Map<String, Object> retrievePlayersData(ProxiedPlayer player) {
        String[] columnNames = {"coins", "channel", "vanished", "join_date"};
        Map<String, Object> playerData = new HashMap<>();

        try (Connection connection = DatabasePool.getConnection();
             ResultSet resultSet = DatabaseUtils.executeQuery(connection, "players", columnNames, "uuid = ?", player.getUniqueId())) {
            while (resultSet.next()) {
                playerData.put("coins", resultSet.getInt(1));
                playerData.put("channel", Channels.valueOf(resultSet.getString(2)));
                playerData.put("vanished", VanishState.valueOf(resultSet.getString(3)));
                playerData.put("join_date", LocalDate.parse(resultSet.getString(4)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerData;
    }

//    private Profile retrieveProfileData(ProxiedPlayer player){
//        String[] columnNames = {"bio", "pronouns"};
//        String bio = null;
//        String pronouns = null;
//        try (Connection connection = DatabasePool.getConnection();
//             ResultSet resultSet = DatabaseUtils.executeQuery(connection, "profile", columnNames, "uuid = ?", player.getUniqueId())) {
//            while (resultSet.next()) {
//                bio = resultSet.getString(1);
//                pronouns = resultSet.getString(2);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    protected void setPermissionMessage(String permissionMessage) {
        super.setPermissionMessage(Messages.NO_PERMISSION.getMessage().toString());
    }
}
