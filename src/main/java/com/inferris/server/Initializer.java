package com.inferris.server;

import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.player.Channels;
import com.inferris.player.vanish.VanishState;
import com.inferris.util.ConfigUtils;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class Initializer {

    // TODO: Marked for complete redo

    @Deprecated
    public void loadPlayerRegistry() {
        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement query = connection.prepareStatement("SELECT * FROM player_data")) {

            ResultSet resultSet = query.executeQuery();

            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid");
                String username = resultSet.getString("username");
                int vanished = resultSet.getInt("vanished");
                VanishState vanishState = VanishState.DISABLED;

                if (vanished == 1) {
                    vanishState = VanishState.ENABLED;
                }

                Configuration playersConfiguration = Inferris.getPlayersConfiguration();
                Channels channel;

                /* For broken configuration, it will fix itself */

                if (playersConfiguration.get("players." + uuid + ".channel") == null) {
                    playersConfiguration.set("players." + uuid + ".channel", "NONE");
                    //playersConfiguration.set("players." + uuid + ".vanish", "DISABLED");
                } else {

                    /* Load channel from config */

                    channel = Channels.valueOf(playersConfiguration.getString("players." + uuid + ".channel"));
                    //vanishState = VanishState.valueOf(playersConfiguration.getString("players." + uuid + ".vanish"));
                    //RegistryManager.getPlayerRegistryCache().put(UUID.fromString(uuid), new Registry(UUID.fromString(uuid), username, channel, vanishState));

                    JedisPool pool = new JedisPool("localhost", Ports.JEDIS.getPort());

                }
                ConfigUtils configUtils = new ConfigUtils();
                ConfigUtils.saveConfiguration(Inferris.getPlayersFile(), playersConfiguration);
                ConfigUtils.reloadConfiguration(ConfigUtils.Types.PLAYERS);
            }

            Inferris.getInstance().getLogger().warning("Player registry loaded successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
