package com.inferris.commands;

import com.google.inject.Inject;
import com.inferris.Inferris;
import com.inferris.player.channel.Channel;
import com.inferris.player.channel.ChannelManager;
import com.inferris.player.PlayerData;
import com.inferris.player.manager.ManagerContainer;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class CommandRemoveFromRedis extends Command {
    private final PlayerDataService playerDataService;
    private final ManagerContainer managerContainer;

    @Inject
    public CommandRemoveFromRedis(String name, PlayerDataService playerDataService, ManagerContainer managerContainer) {
        super("removefromredis");
        this.playerDataService = playerDataService;
        this.managerContainer = managerContainer;
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            return playerDataService.getPlayerData(player.getUniqueId()).getRank().getBranchValue(Branch.STAFF) >= 3;
        }
        return sender.getName().equalsIgnoreCase("CONSOLE");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        ChannelManager channelManager = managerContainer.getChannelManager();

        if (length == 0) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatUtil.translateToHex(
                    """
                            #ff1100Warning! The /removefromredis command will irreversibly delete an account's data from the Redis keystore. This action cannot be undone.
                            Before proceeding, you MUST request and receive explicit permission.
                            """
            )));
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage: /removefromredis <player>"));
            return;
        }

        UUID uuid;
        PlayerData playerData;
        try {
            uuid = playerDataService.fetchUUIDByUsername(args[0]);
            playerData = playerDataService.fetchPlayerDataFromDatabase(uuid);
        } catch (Exception e) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "An error occurred while retrieving player data: " + e.getMessage()));
            return;
        }

        if (playerData == null) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "Player data could not be found for the given UUID."));
            return;
        }

        try (Jedis jedis = Inferris.getJedisPool().getResource()) {
            jedis.hdel("playerdata", uuid.toString());

            if (sender instanceof ProxiedPlayer player) {
                channelManager.sendStaffChatMessage(Channel.STAFF, player.getName() + ChatColor.YELLOW + " removed " + playerData.getRank().getByBranch().getPrefix(true)
                        + ChatColor.RESET + playerData.getUsername() + ChatColor.YELLOW + " from Redis keystore", ChannelManager.StaffChatMessageType.NOTIFICATION);
            } else {
                channelManager.sendStaffChatMessage(Channel.STAFF, ChatColor.RED + sender.getName() + ChatColor.YELLOW + " removed " + playerData.getRank().getByBranch().getPrefix(true)
                        + ChatColor.RESET + playerData.getUsername() + ChatColor.YELLOW + " from Redis keystore", ChannelManager.StaffChatMessageType.NOTIFICATION);
            }

            sender.sendMessage(new TextComponent(ChatColor.GREEN + "Redis data has been successfully deleted."));

            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(uuid);
            if (target != null) {
                if (ProxyServer.getInstance().getPlayer(uuid).isConnected()) {
                    TextComponent textComponent = new TextComponent(ChatColor.RED + "Your Redis keystore data has been deleted by an admin." +
                            "\n\n\nIf you believe this was done in error,\nplease contact support.");
                    target.disconnect(textComponent);
                }
            }
        }
    }
}
