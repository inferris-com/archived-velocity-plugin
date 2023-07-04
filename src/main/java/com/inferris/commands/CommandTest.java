package com.inferris.commands;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.inferris.*;
import com.inferris.SerializationModule;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.player.registry.RegistryManager;
import com.inferris.server.BungeeChannel;
import com.inferris.server.Initializer;
import com.inferris.server.Ports;
import com.inferris.server.Subchannel;
import com.inferris.util.BungeeUtils;
import com.inferris.util.CacheSerializationUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.UUID;

public class CommandTest extends Command {

    public CommandTest(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if (length == 1) {
                if (args[0].equalsIgnoreCase("registry")) {
                    JedisPool pool = Inferris.getJedisPool();

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new SerializationModule());
                    objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                    try (Jedis jedis = pool.getResource()) {
                        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
                        player.sendMessage(playerData.getRegistry().getUsername());
                        player.sendMessage(playerData.getRegistry().toString());
                        player.sendMessage(playerData.getVanishState().toString());
                        player.sendMessage(playerData.getChannel().getMessage());

                    }
                }
                if (args[0].equalsIgnoreCase("registry2")) {
//                    for (UUID uuid : RegistryManager.getPlayerRegistryCache().asMap().keySet()) {
//                        player.sendMessage(new TextComponent(uuid.toString()));
//                    }
                }
                if (args[0].equalsIgnoreCase("players")) {
                    Configuration configuration = Inferris.getPlayersConfiguration().getSection("players");
                    player.sendMessage(new TextComponent(configuration.get(player.getUniqueId() + ".channel").toString()));
                }
                if(args[0].equalsIgnoreCase("redis")){
                    try(Jedis jedis = Inferris.getJedisPool().getResource()){
                        jedis.publish("playerdata_update", CacheSerializationUtils.serializePlayerData(PlayerDataManager.getInstance().getPlayerData(player)));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (args[0].equalsIgnoreCase("test")) {
                    BungeeUtils.sendBungeeMessage(player, BungeeChannel.TEST, Subchannel.FORWARD, "Hi");
                }
            }

            if (length == 2) {
                if (args[0].equalsIgnoreCase("perms")) {
                    List<String> adminPermissions = Inferris.getPermissionsConfiguration().getStringList("ranks." + args[1]);
                    player.sendMessage(new TextComponent(adminPermissions.toString()));

                } else if (args[0].equalsIgnoreCase("registry")) {
                    if (args[1].equalsIgnoreCase("invalidate")) {
                        RegistryManager.getInstance().deleteRegistry();

                        player.sendMessage(new TextComponent(ChatColor.GREEN + "Invalidated registry"));

                    } else if (args[1].equalsIgnoreCase("reload")) {
                        RegistryManager.getInstance().deleteRegistry();
                        new Initializer().loadPlayerRegistry();
                        player.sendMessage(new TextComponent(ChatColor.GREEN + "Reloaded registry"));
                    }
                }
            }

            if (length == 3) {
                if (args[0].equalsIgnoreCase("registry") && args[1].equalsIgnoreCase("remove")) {
                    UUID uuid = UUID.fromString(args[2]);
                    RegistryManager.getInstance().invalidateEntry(UUID.fromString(args[2]));
                    player.sendMessage(new TextComponent(ChatColor.GREEN + "Removed " + uuid + " from registry"));
                }
            }


            if (length == 4) {
                if (args[0].equalsIgnoreCase("registry")) {
                    //debug 1.registry 2.add 3.uuid 4.username
                    if (args[1].equalsIgnoreCase("add")) {
                        UUID uuid = UUID.fromString(args[2]);
                        String username = args[3];
                        if (player.getUniqueId().equals(uuid)) {
                            RegistryManager.getInstance().invalidateEntry(player.getUniqueId());
                        }
                       // RegistryManager.getInstance().addToRegistryDefault(ProxyServer.getInstance().getPlayer(args[3]));
                        player.sendMessage(new TextComponent(ChatColor.GREEN + "Added " + username + " to registry"));
                    }
                }
            }
        }else{
            JedisPool jedisPool = new JedisPool("localhost", Ports.JEDIS.getPort()); // Set Redis server details

        }
    }
}
