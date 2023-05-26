package com.inferris.rank;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import com.inferris.player.registry.RegistryManager;
import com.inferris.server.Ports;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RanksManager {
    private static RanksManager instance;
    private final JedisPool jedisPool;

    private RanksManager() {
        jedisPool = new JedisPool("localhost", Ports.JEDIS.getPort()); // Set Redis server details
    }

    public static RanksManager getInstance() {
        if(instance == null){
            instance = new RanksManager();
        }
        return instance;
    }

    public Rank getRank(ProxiedPlayer player) {
        return loadRanks(player);
    }

    public Rank loadRanks(ProxiedPlayer player){
        Inferris.getInstance().getLogger().warning("Loading ranks");
        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT staff, donor, other FROM ranks WHERE `uuid` = ?");
             PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO ranks (uuid, staff, donor, other) VALUES (?,?,?,?)")){
            statement.setString(1, String.valueOf(player.getUniqueId()));
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                int staff = rs.getInt("staff");
                int donor = rs.getInt("donor");
                int other = rs.getInt("other");
                return new Rank(staff, donor, other);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return new Rank(0, 0, 0);
    }

    private Rank createEmpty(ProxiedPlayer player) {
        // Create and return an empty Registry object with default values
        return new Rank(0, 0, 0);
    }

    public void invalidateEntry() {
        try (Jedis jedis = RegistryManager.getInstance().getJedisPool().getResource()) {
            jedis.del("playerdata");
        }
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}
