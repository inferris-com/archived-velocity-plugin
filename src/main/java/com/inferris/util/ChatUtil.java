package com.inferris.util;

import com.inferris.messaging.PlayerCondition;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    public static void sendGlobalMessage(PlayerCondition condition, BaseComponent[] message) {
        TextComponent textComponent = new TextComponent(message);

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            try {
                if (condition.shouldSendMessage(player)) {
                    player.sendMessage(textComponent);
                }
            } catch (Exception e) {
                // Silently block the error
            }
        }
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

