package com.inferris.rank;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.inferris.Inferris;
import com.inferris.database.DatabasePool;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RanksManager {
    private static RanksManager instance;
    private final Cache<UUID,Rank> rankCache;

    private RanksManager() {
        rankCache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
    }

    public static RanksManager getInstance() {
        if(instance == null){
            instance = new RanksManager();
        }
        return instance;
    }

    public void cacheRank(ProxiedPlayer player, Rank rank){
        rankCache.put(player.getUniqueId(), rank);
    }

    public Rank getRank(ProxiedPlayer player){
        try{
            return rankCache.get(player.getUniqueId(), () -> {
                Inferris.getInstance().getLogger().severe("Rank not found in cache");
                return loadRanks(player);
            });
        }catch(ExecutionException e){
            e.printStackTrace();
        }
        return new Rank(0, 0 ,0);
    }

    public Cache<UUID, Rank> getRankCache() {
        return rankCache;
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

    public void invalidate(ProxiedPlayer player){
        rankCache.invalidate(player.getUniqueId());
    }
}
