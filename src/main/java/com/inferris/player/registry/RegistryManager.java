package com.inferris.player.registry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inferris.Inferris;
import com.inferris.SerializationModule;
import com.inferris.database.DatabasePool;
import com.inferris.player.Channels;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.server.Ports;
import com.inferris.util.CacheSerializationUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class RegistryManager {
    //todo Look over class
    private static RegistryManager instance;
    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;


    private RegistryManager() {
        jedisPool = new JedisPool("localhost", Ports.JEDIS.getPort()); // Set Redis server details

        objectMapper = CacheSerializationUtils.createObjectMapper(new SerializationModule());
    }

    public static RegistryManager getInstance() {
        if (instance == null) {
            instance = new RegistryManager();
        }
        return instance;
    }

    public void addPlayer(ProxiedPlayer player, Registry registry) {
        try (Jedis jedis = jedisPool.getResource()) {
            String json = serializeRegistry(registry);
            jedis.set(player.getUniqueId().toString(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Registry getRegistry(ProxiedPlayer player, Rank rank){
        Inferris.getInstance().getLogger().info("getRegistry() triggered");

        try (Connection connection = DatabasePool.getConnection();
             PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?");
             PreparedStatement insertPlayersStatement = connection.prepareStatement("INSERT INTO players (uuid, username, coins, channel, vanished, join_date) VALUES (?, ?, ?, ?, ?, ?)");
             PreparedStatement insertProfileStatement = connection.prepareStatement("INSERT INTO profile (uuid, bio, pronouns) VALUES (?, ?, ?)");
             PreparedStatement updateStatement = connection.prepareStatement("UPDATE players SET username = ? WHERE uuid = ?")) {

            queryStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = queryStatement.executeQuery();

            /* If they are in the database */

            if(resultSet.next()) {
                String storedUsername = resultSet.getString("username");
                int vanished = resultSet.getInt("vanished");
                VanishState vanishState = VanishState.DISABLED;

                if(vanished == 1 || rank.getBranchID(Branch.STAFF) >=3){
                    vanishState = VanishState.ENABLED;
                }
                Inferris.getInstance().getLogger().info("Properly in table");

                if(!storedUsername.equals(player.getName())){
                    updateStatement.setString(1, player.getName());
                    updateStatement.setString(2, player.getUniqueId().toString());
                    updateStatement.executeUpdate();
                    Inferris.getInstance().getLogger().warning("Updated username (getRegistry)");
                }

                return new Registry(player.getUniqueId(), player.getName());

            }else{
                Inferris.getInstance().getLogger().warning("Inserting into table.");
                insertPlayersStatement.setString(1, player.getUniqueId().toString());
                insertPlayersStatement.setString(2, player.getName());
                insertPlayersStatement.setInt(3, 36);
                insertPlayersStatement.setString(4, String.valueOf(Channels.NONE));
                insertPlayersStatement.setInt(5, 0);
                insertPlayersStatement.setObject(6, LocalDate.now());
                insertPlayersStatement.execute();

                insertProfileStatement.setString(1, player.getUniqueId().toString());;
                insertProfileStatement.setString(2, null);
                insertProfileStatement.setString(3, null);
                insertProfileStatement.execute();

                Inferris.getInstance().getLogger().severe("Added player to table");
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return new Registry(player.getUniqueId(), player.getName());
    }


    private Registry createEmptyRegistry(ProxiedPlayer player) {
        // Create and return an empty Registry object with default values
        return new Registry(player.getUniqueId(), player.getName());
    }

    public Map<String, Registry> getAllRegistryEntries() {
        Map<String, Registry> registryMap = new HashMap<>();
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> registryJsonMap = jedis.hgetAll("registry");
            for (Map.Entry<String, String> entry : registryJsonMap.entrySet()) {
                String uuid = entry.getKey();
                String registryJson = entry.getValue();
                Registry registry = CacheSerializationUtils.deserializeRegistry(registryJson);
                registryMap.put(uuid, registry);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return registryMap;
    }

    public void deleteRegistry(){
        try (Jedis jedis = RegistryManager.getInstance().getJedisPool().getResource()) {
            jedis.del("registry");
        }

    }

    public void invalidateEntry(UUID uuid) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel("registry", uuid.toString());
        }
    }

    private String serializeRegistry(Registry registry) throws JsonProcessingException {
        return objectMapper.writeValueAsString(registry);
    }

    private Registry deserializeRegistry(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Registry.class);
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}