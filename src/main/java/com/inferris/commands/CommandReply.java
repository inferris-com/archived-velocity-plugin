package com.inferris.commands;

import com.inferris.commands.cache.CommandMessageCache;
import com.inferris.player.*;
import com.inferris.player.vanish.VanishState;
import com.inferris.rank.Branch;
import com.inferris.server.Message;
import com.inferris.util.MessageUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandReply extends Command implements TabExecutor {
    private final PlayerDataService playerDataService;
    public CommandReply(String name, PlayerDataService playerDataService) {
        super(name, null, "r");
        this.playerDataService = playerDataService;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;

        if (sender instanceof ProxiedPlayer player) {
            if (length == 0) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Usage: /reply <message>"));
                return;
            }
            /* If the receiver from main sender, you, is in the cache
             * As a reminder, you are KEY, receiver (original sender) is VALUE
             * The replier, you, gets the reply attachment */

            UUID senderUUID = player.getUniqueId();
            CommandMessageCache cache = CommandMessage.getCacheReplyHandler().getIfPresent(senderUUID);
            String message = String.join(" ", Arrays.copyOfRange(args, 0, length));


            if (cache == null) {
                player.sendMessage(new TextComponent(ChatColor.RED + "Your reply cache has expired. Use /message instead."));
                return;
            }

            UUID targetUUID = cache.getCache().asMap().get(senderUUID);
            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(targetUUID);
            PlayerData playerData = playerDataService.getPlayerData(player.getUniqueId());
            PlayerContext playerContext = PlayerContextFactory.create(senderUUID, playerDataService);

            if (target == null) {
                player.sendMessage(new TextComponent(Message.COULD_NOT_FIND_PLAYER.getMessage()));
                return;
            }

            // Check if the target player is vanished
            if (PlayerDataManager.getInstance().getPlayerData(target).getVanishState() == VanishState.ENABLED) {
                if (playerContext.getRank().getBranchValue(Branch.STAFF) < 3) {
                    player.sendMessage(new TextComponent(Message.COULD_NOT_FIND_PLAYER.getMessage()));
                    target.sendMessage(new TextComponent(ChatColor.GRAY + "Notice: " + playerContext.getRank().getByBranch() + " " + player.getName() + ChatColor.GRAY
                            + " attempted to message you: " + message));
                    return;
                }
            }

            MessageUtil.sendMessage(player, target, message);
            CommandMessage.getCacheReplyHandler().invalidate(targetUUID);
            CommandMessageCache targetCache = CommandMessage.getCacheReplyHandler().asMap().computeIfAbsent(targetUUID,
                    uuid -> new CommandMessageCache(target, player, 5L, TimeUnit.MINUTES));
            targetCache.add();
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {

        if (args.length == 1 && sender instanceof ProxiedPlayer player) {
            String partialPlayerName = args[0];
            List<String> playerNames = new ArrayList<>();
            for (ProxiedPlayer proxiedPlayers : ProxyServer.getInstance().getPlayers()) {
                if (!(PlayerDataManager.getInstance().getPlayerData(proxiedPlayers).getVanishState() == VanishState.ENABLED)) {
                    String playerName = proxiedPlayers.getName();
                    if (playerName.toLowerCase().startsWith(partialPlayerName.toLowerCase())) {
                        playerNames.add(playerName);
                    }
                }
            }
            return playerNames;
        }
        return Collections.emptyList();
    }
}