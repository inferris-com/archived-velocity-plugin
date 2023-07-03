package com.inferris.util;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ChatUtil {
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

    public static HoverEvent createHoverEvent(String text) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(text));
    }

    public static ClickEvent createClickEvent(String value, ClickEvent.Action action) {
        return new ClickEvent(action, value);
    }
}
