package com.inferris.util;

import com.inferris.Inferris;
import com.inferris.events.redis.EventPayload;
import com.inferris.events.redis.PlayerAction;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.ServiceLocator;
import com.inferris.rank.RankRegistry;
import com.inferris.server.Tag;
import com.inferris.server.jedis.JedisChannel;
import com.inferris.server.jedis.JedisHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MessageUtil {

    public static void sendMessage(ProxiedPlayer sender, ProxiedPlayer receiver, String message) {
        PlayerDataService playerDataService = ServiceLocator.getPlayerDataService();
        RankRegistry playerRank = playerDataService.getPlayerData(sender.getUniqueId()).getRank().getByBranch();
        RankRegistry receiverRank = playerDataService.getPlayerData(receiver.getUniqueId()).getRank().getByBranch();
        sender.sendMessage(new TextComponent(ChatColor.GREEN + "Message sent!"));
        sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GRAY + "To " + receiverRank.getPrefix(true) + ChatColor.RESET + receiver.getName() + ": " + message));
        receiver.sendMessage(TextComponent.fromLegacyText(ChatColor.GRAY + "From " + playerRank.getPrefix(true) + ChatColor.RESET + sender.getName() + ": " + message));
        EventPayload payload = new EventPayload(receiver.getUniqueId(), PlayerAction.NOTIFY, "ENTITY_CHICKEN_EGG", Inferris.getInstanceId());
        JedisHelper.publish(JedisChannel.PLAYER_FLEX_EVENT, payload.toPayloadString());
    }

    public static void sendMessage(ProxiedPlayer player, BaseComponent message) {
        player.sendMessage(message);
    }

    public static void sendMessage(ProxiedPlayer player, String message){
        player.sendMessage(new TextComponent(message));
    }
    public static void sendMessage(ProxiedPlayer player, String message, boolean isStaff){
        player.sendMessage( new TextComponent(Tag.STAFF.getName(true) + message));
    }
}
