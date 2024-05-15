package com.inferris.commands;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.inferris.*;
import com.inferris.rank.Branch;
import com.inferris.serialization.SerializationModule;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Rank;
import com.inferris.util.SerializationUtils;
import com.inferris.util.ChatUtil;
import io.tebex.BuycraftApi;
import io.tebex.exception.BuycraftException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CommandBungeeTest extends Command {

    public CommandBungeeTest(String name) {
        super(name);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return PlayerDataManager.getInstance().getPlayerData((ProxiedPlayer) sender).getBranchValue(Branch.STAFF) >= 3;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if (length == 1) {
                if(args[0].equalsIgnoreCase("playerdata")){
                    PlayerData playerData  = PlayerDataManager.getInstance().getRedisData(player);
                    try {
                        player.sendMessage(new TextComponent(SerializationUtils.serializePlayerData(playerData)));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(args[0].equalsIgnoreCase("cache")){
                    PlayerData playerData  = PlayerDataManager.getInstance().getPlayerData(player);
                    try {
                        player.sendMessage(new TextComponent(SerializationUtils.serializePlayerData(playerData)));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(args[0].equalsIgnoreCase("buycraft")){
                    try {
                        BuycraftApi api = new BuycraftApi("707b2c9774328e8b857424a3a3811d874f77e482");
                    } catch (BuycraftException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(args[0].equalsIgnoreCase("colors")){
                    player.sendMessage(TextComponent.fromLegacyText(ChatUtil.translateToHex("&8[" + "#FF5733" + "Admin" + "&8]")));
                    player.sendMessage(TextComponent.fromLegacyText(ChatUtil.translateToHex("&8[" + "#007BFF" + "Admin" + "&8]")));
                    player.sendMessage(TextComponent.fromLegacyText(ChatUtil.translateToHex("&8[" + "#FFD700" + "Admin" + "&8]")));
                    player.sendMessage(TextComponent.fromLegacyText(ChatUtil.translateToHex("&8[" + "#4CAF50" + "Admin" + "&8]")));
                    player.sendMessage(TextComponent.fromLegacyText(ChatUtil.translateToHex("&8[" + "#8B008B" + "Admin" + "&8]")));
                    player.sendMessage(TextComponent.fromLegacyText(ChatUtil.translateToHex("&8[" + "#1E90FF" + "Admin" + "&8]")));
                    player.sendMessage(TextComponent.fromLegacyText(ChatUtil.translateToHex("&8[" + "#FF1493" + "Admin" + "&8]")));
                    player.sendMessage(TextComponent.fromLegacyText(ChatUtil.translateToHex("&8[" + "#800000" + "Admin" + "&8]")));
                }
                if (args[0].equalsIgnoreCase("ranks")) {
                    PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
                    Rank rank = playerData.getRank();
                    player.sendMessage(TextComponent.fromLegacyText("By branches - " + playerData.formatRankList(playerData.getApplicableRanks())));
                }
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
                        player.sendMessage(playerData.getUsername());
                        player.sendMessage(playerData.getVanishState().toString());
                        player.sendMessage(playerData.getChannel().getMessage());

                    }
                }
                if (args[0].equalsIgnoreCase("registry2")) {
//                    for (UUID uuid : RegistryManager.getPlayerRegistryCache().asMap().keySet()) {
//                        player.sendMessage(new TextComponent(uuid.toString()));
//                    }
                }
                if (args[0].equalsIgnoreCase("redis")) {
                    try (Jedis jedis = Inferris.getJedisPool().getResource()) {
                        jedis.publish("playerdata_update", SerializationUtils.serializePlayerData(PlayerDataManager.getInstance().getPlayerData(player)));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (args[0].equalsIgnoreCase("server")) {
                    player.sendMessage(new TextComponent(ChatColor.AQUA + PlayerDataManager.getInstance().getPlayerData(player).getCurrentServer().name()));
                }
            }
        }
    }
}
