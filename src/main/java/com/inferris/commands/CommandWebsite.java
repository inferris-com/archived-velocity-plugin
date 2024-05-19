package com.inferris.commands;

import com.inferris.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class CommandWebsite extends Command {
    public CommandWebsite(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        String primaryColor = ChatUtil.translateToHex("#1E90FF", ChatUtil.FormatType.BOLD); // Aqua
        String secondaryColor = ChatUtil.translateToHex("#376cbf"); // Aqua
        ChatColor messageColor = ChatColor.YELLOW; // Yellow
        ChatColor accentColor = ChatColor.DARK_AQUA; // Dark Aqua
        ChatColor headerFooterColor = ChatColor.DARK_GRAY; // Gold
        ChatColor resetColor = ChatColor.RESET; // Reset to default color

        // Symbols
        String sparkle = ChatUtil.translateToHex(" #FFD700✨ ",  ChatUtil.FormatType.BOLD);
        String flower = ChatUtil.translateToHex(" #FF69B4\uD83C\uDF3A ",  ChatUtil.FormatType.BOLD);
        String line = ChatColor.STRIKETHROUGH + "---------------------------------";


        sender.sendMessage(TextComponent.fromLegacyText(ChatUtil.translateToHex("""
                #f59ae7\uD83C\uDF3A #00BFFFInferris website:

                #6E45E2https://inferris.com""")));
    }
}
