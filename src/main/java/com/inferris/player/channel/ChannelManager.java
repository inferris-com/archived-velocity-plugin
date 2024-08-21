package com.inferris.player.channel;

import com.google.inject.Inject;
import com.inferris.Inferris;
import com.inferris.player.service.PlayerDataService;
import com.inferris.player.context.PlayerContext;
import com.inferris.rank.Branch;
import com.inferris.rank.RankRegistry;
import com.inferris.server.Tag;
import com.inferris.util.ChatUtil;
import com.inferris.util.DatabaseUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.SQLException;
import java.util.UUID;

public class ChannelManager {

    private final PlayerDataService playerDataService;

    @Inject
    public ChannelManager(PlayerDataService playerDataService){
        this.playerDataService = playerDataService;
    }

    public void setChannel(ProxiedPlayer player, Channel channel, boolean sendMessage){
        playerDataService.updatePlayerData(player.getUniqueId(), playerData -> {
            playerData.setChannel(channel);
        });
        String channelName = null;

        if(sendMessage){
            switch (channel){
                case STAFF -> channelName = ChatColor.AQUA + String.valueOf(channel);
                case ADMIN -> channelName = ChatColor.RED + String.valueOf(channel);
                case SPECIAL -> channelName = ChatColor.GOLD + String.valueOf(channel);
                case NONE -> channelName = ChatColor.GRAY + String.valueOf(channel);
            }
            player.sendMessage(new TextComponent(ChatColor.YELLOW + "Channel set to " + channelName));
        }

        String sql = "UPDATE player_data SET channel = ? WHERE uuid = ?";
        Object[] parameters = {channel.toString(), player.getUniqueId().toString()};
            try {
                DatabaseUtils.executeUpdate(sql, parameters);
            }catch(SQLException e){
                Inferris.getInstance().getLogger().severe(e.getMessage());
            }
    }

    // Method for sending player messages
    public void sendStaffChatMessage(Channel channel, String message, StaffChatMessageType chatMessageType, UUID senderUuid) {
        if (chatMessageType == StaffChatMessageType.PLAYER && senderUuid != null) {
            BaseComponent[] textComponent = createTextComponent(channel, message, chatMessageType, senderUuid);
            sendMessageToStaff(senderUuid, channel, textComponent);
        } else {
            throw new IllegalArgumentException("Invalid use of sendStaffChatMessage for PLAYER type without senderUuid.");
        }
    }

    // Method for sending console and notification messages
    public void sendStaffChatMessage(Channel channel, String message, StaffChatMessageType chatMessageType) {
        if (chatMessageType == StaffChatMessageType.CONSOLE || chatMessageType == StaffChatMessageType.NOTIFICATION) {
            BaseComponent[] textComponent = createTextComponent(channel, message, chatMessageType, null);
            sendMessageToStaff(null, channel, textComponent);
        } else {
            throw new IllegalArgumentException("Invalid use of sendStaffChatMessage for non-console/notification type.");
        }
    }

    // Helper method to create text components based on the message type
    private BaseComponent[] createTextComponent(Channel channel, String message, StaffChatMessageType chatMessageType, UUID senderUuid) {
        String formattedMessage;
        switch (chatMessageType) {
            case PLAYER:
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(senderUuid);
                UUID uuid = player.getUniqueId();
                PlayerContext playerContext = new PlayerContext(player.getUniqueId(), playerDataService);

                RankRegistry rank = playerDataService.getPlayerData(uuid).getRank().getByBranch();
                formattedMessage = channel.getTag(true) +
                        rank.getPrefix(true) + playerContext.getNameColor() + player.getName() +
                        ChatColor.RESET + ": " + message;
                break;
            case CONSOLE:
                formattedMessage = Tag.STAFF.getName(true) +
                        ChatColor.RED + ChatColor.ITALIC + "Terminal" +
                        ChatColor.RESET + ": " + message;
                break;
            case NOTIFICATION:
            default:
                formattedMessage = Tag.STAFF.getName(true) + ChatColor.RESET + message;
                break;
        }
        return TextComponent.fromLegacyText(formattedMessage);
    }

    // Helper method to send the message to all staff members and the console
    private void sendMessageToStaff(UUID senderUuid, Channel channel, BaseComponent[] textComponent) {
        if (senderUuid == null) {
            // Handle message sending for the console
            ChatUtil.sendGlobalMessage(player -> switch (channel) {
                case STAFF, ADMIN -> true;
                default -> false;
            }, textComponent);
        } else {
            // Handle message sending for players
            ChatUtil.sendGlobalMessage(player -> {
                PlayerContext playerContext = new PlayerContext(player.getUniqueId(), playerDataService);
                switch (channel) {
                    case STAFF -> {
                        return playerContext.getRank().getBranchValue(Branch.STAFF) >= 1;
                    }
                    case ADMIN -> {
                        return playerContext.getRank().getBranchValue(Branch.STAFF) >= 3;
                    }
                    default -> {
                        return false;
                    }
                }
            }, textComponent);
        }
        ProxyServer.getInstance().getConsole().sendMessage(textComponent);
    }

    public enum StaffChatMessageType {
        PLAYER,
        CONSOLE,
        NOTIFICATION;
    }
}
