package com.inferris.commands;

import com.inferris.player.*;
import com.inferris.rank.Branch;
import com.inferris.rank.Rank;
import com.inferris.server.ErrorCode;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandBungeeDev extends Command {
    private final PlayerDataService playerDataService;

    public CommandBungeeDev(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer player) {
            int length = args.length;

            if (length == 0) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Command usage is not available here due to how unstable the command can be."));
                return;
            }
            if (length == 1) {
                String action = args[0].toLowerCase();
                switch (action) {
                    case "end" -> {
                        ProxyServer.getInstance().stop(ChatColor.GRAY + "Woa! An issue has occurred: " + ErrorCode.PROXY_STOPPED_BY_ADMIN.getCode(true)
                                + "\n\n" + ErrorCode.PROXY_STOPPED_BY_ADMIN.getMessage(true) + "\n\n"
                                + ChatColor.WHITE + "Not to fret! They're probably fixin' up an issue\n or deploying a patch. Hang tight!");
                    }
                    case "cache" -> {
                        player.sendMessage(new TextComponent(String.valueOf(PlayerDataManager.getInstance().getCache().getIfPresent(player.getUniqueId()).getProfile().toString())));
                    }
                }
            }
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            PlayerDataService playerDataService = ServiceLocator.getPlayerDataService();
            PlayerContext playerContext = PlayerContextFactory.create(player.getUniqueId(), playerDataService);
            Rank rank = playerContext.getRank();
            return rank.getBranchValue(Branch.STAFF) >= 3 || player.getUniqueId().toString().equals("7d16b15d-bb22-4a6d-80db-6213b3d75007");
        }
        return false;
    }
}
