package com.inferris.commands;

import com.inferris.player.*;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.context.PlayerContextFactory;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandAnnouncement extends Command {
    private final PlayerDataService playerDataService;

    public CommandAnnouncement(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        if (args.length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /announce <message>"));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (String word : args) {
            message.append(word).append(" "); // Add a space between words
        }

        // Colors

        String primaryColor = ChatUtil.translateToHex("#1E90FF", ChatUtil.FormatType.BOLD); // Aqua
        String secondaryColor = ChatUtil.translateToHex("#376cbf"); // Aqua
        ChatColor messageColor = ChatColor.YELLOW; // Yellow
        ChatColor accentColor = ChatColor.DARK_AQUA; // Dark Aqua
        ChatColor headerFooterColor = ChatColor.DARK_GRAY; // Gold
        ChatColor resetColor = ChatColor.RESET; // Reset to default color

        // Symbols
        String sparkle = ChatUtil.translateToHex(" #FFD700✨ ", ChatUtil.FormatType.BOLD);
        String flower = ChatUtil.translateToHex(" #FF69B4\uD83C\uDF3A ", ChatUtil.FormatType.BOLD);
        String line = ChatColor.STRIKETHROUGH + "---------------------------------";

        // Announcement Message
        String announcementMessage =
                headerFooterColor + line + "\n" +
                        accentColor + " " + sparkle + " " + primaryColor + "Inferris Announcement" + " " + sparkle + "\n" +
                        headerFooterColor + line + "\n\n" +
                        messageColor + ChatUtil.translateToHex(message.toString().trim()) + "\n\n" +
                        headerFooterColor + line + "\n" +
                        accentColor + " " + flower + " " + secondaryColor + "Thank you for your attention!" + " " + flower + "\n" +
                        headerFooterColor + line;


        for (String word : args) {
            message.append(word).append(" "); // Add a space between words
        }

        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            player.sendMessage(TextComponent.fromLegacyText(announcementMessage));
        }
    }


    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            PlayerDataService playerDataService = ServiceLocator.getPlayerDataService();
            PlayerContext playerContext = PlayerContextFactory.create(player.getUniqueId(), playerDataService);
            return playerContext.getRank().getBranchValue(Branch.STAFF) >= 3;
        }
        return false;
    }
}
