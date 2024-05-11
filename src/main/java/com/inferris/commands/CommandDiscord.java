package com.inferris.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.inferris.Inferris;
import com.inferris.player.discord.UserFlag;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class CommandDiscord extends Command {
    public CommandDiscord(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if (length == 0) {
                player.sendMessage(new TextComponent(ChatColor.GREEN + "Verify your Discord account with your Minecraft account at " + ChatColor.AQUA + "https://localhost:53134/auth?uuid=" + player.getUniqueId().toString()));
            }
            if(length == 1){
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /discord verify <code>"));

            }

            if (length == 2) {
                if (args[0].equalsIgnoreCase("verify")){
                    String code = args[1];

                    try(Jedis jedis = Inferris.getJedisPool().getResource()){
                        String jsonData = jedis.hget("temp_discord_verification", player.getUniqueId().toString());
                        if (jsonData != null) {
                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode rootNode = objectMapper.readTree(jsonData);
                            String uuid = rootNode.path("uuid").asText();
                            String discordId = rootNode.path("discordId").asText();
                            String username = rootNode.path("username").asText();
                            int flags = rootNode.path("flags").asInt();
                            String verificationCode = rootNode.path("verificationCode").asText();

                            player.sendMessage(uuid);
                            player.sendMessage(discordId);
                            player.sendMessage(username);
                            player.sendMessage(UserFlag.getUserFlagDescriptions(flags).toString());
                            player.sendMessage(verificationCode);
                            if(player.getUniqueId().toString().equalsIgnoreCase(uuid)){
                                if(code.equalsIgnoreCase(verificationCode)){
                                    jedis.hdel("temp_discord_verification", player.getUniqueId().toString());

                                    ObjectNode permanentData = objectMapper.createObjectNode();
                                    permanentData.put("uuid", player.getUniqueId().toString());
                                    permanentData.put("discordId", discordId);
                                    permanentData.put("username", username);
                                    permanentData.put("flags", flags);

                                    jedis.hset("verified_discord_users", player.getUniqueId().toString(), permanentData.toString());

                                    player.sendMessage(new TextComponent(ChatColor.GREEN + "Discord account linked! Enjoy!"));
                                }else{
                                    player.sendMessage(new TextComponent(ChatColor.RED + "Error: invalid code!"));
                                }
                            }
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
