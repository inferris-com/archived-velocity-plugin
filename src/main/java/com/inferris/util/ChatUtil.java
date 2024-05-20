package com.inferris.util;

import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.RankRegistry;
import com.inferris.server.Tag;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    // Method for sending player messages
    public static void sendStaffChatMessage(String message, StaffChatMessageType chatMessageType, UUID senderUuid) {
        if (chatMessageType == StaffChatMessageType.PLAYER && senderUuid != null) {
            BaseComponent[] textComponent = createTextComponent(message, chatMessageType, senderUuid);
            sendMessageToStaff(textComponent);
        } else {
            throw new IllegalArgumentException("Invalid use of sendStaffChatMessage for PLAYER type without senderUuid.");
        }
    }

    // Method for sending console and notification messages
    public static void sendStaffChatMessage(String message, StaffChatMessageType chatMessageType) {
        if (chatMessageType == StaffChatMessageType.CONSOLE || chatMessageType == StaffChatMessageType.NOTIFICATION) {
            BaseComponent[] textComponent = createTextComponent(message, chatMessageType, null);
            sendMessageToStaff(textComponent);
        } else {
            throw new IllegalArgumentException("Invalid use of sendStaffChatMessage for non-console/notification type.");
        }
    }

    // Helper method to create text components based on the message type
    private static BaseComponent[] createTextComponent(String message, StaffChatMessageType chatMessageType, UUID senderUuid) {
        String formattedMessage;
        switch (chatMessageType) {
            case PLAYER:
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(senderUuid);
                PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(senderUuid);
                RankRegistry rank = playerData.getByBranch();
                formattedMessage = Tag.STAFF.getName(true) +
                        rank.getPrefix(true) + playerData.getNameColor() + player.getName() +
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
    private static void sendMessageToStaff(BaseComponent[] textComponent) {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (PlayerDataManager.getInstance().getPlayerData(player).isStaff()) {
                player.sendMessage(textComponent);
            }
        }
        ProxyServer.getInstance().getConsole().sendMessage(textComponent);
    }

    public enum StaffChatMessageType{
        PLAYER,
        CONSOLE,
        NOTIFICATION;
    }

    public static String translateToHex(String message) {
        final Pattern pattern = Pattern.compile("(?<!\\\\)(#[a-fA-F0-9]{6})");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String translateToHex(String message, FormatType type) {
        final Pattern pattern = Pattern.compile("(?<!\\\\)(#[a-fA-F0-9]{6})");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }
            switch (type) {
                case BOLD -> builder.append(ChatColor.BOLD);
                case ITALICS -> builder.append(ChatColor.ITALIC);
            }
            builder.append(ChatColor.BOLD);

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static TextComponent createTextComponent(String prefix, String content) {
        TextComponent component = new TextComponent(prefix);
        component.addExtra(content);
        return component;
    }

    public static TextComponent createClickableTextComponent(String prefix, String hoverText, String command, ClickEvent.Action action) {
        TextComponent component = new TextComponent(prefix);
        component.setHoverEvent(createHoverEvent(hoverText));
        component.setClickEvent(createClickEvent(command, action));
        return component;
    }

    public static TextComponent createLegacyClickableTextComponent(String prefix, String hoverText, String command, ClickEvent.Action action) {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(prefix));
        component.setHoverEvent(createHoverEvent(hoverText));
        component.setClickEvent(createClickEvent(command, action));
        return component;
    }

    public static HoverEvent createHoverEvent(String text) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(text));
    }

    public static ClickEvent createClickEvent(String value, ClickEvent.Action action) {
        return new ClickEvent(action, value);
    }

    public enum FormatType {
        BOLD,
        ITALICS;
    }
}

