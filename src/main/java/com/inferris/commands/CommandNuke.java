package com.inferris.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import com.inferris.common.ColorType;
import com.inferris.player.channel.Channel;
import com.inferris.player.channel.ChannelManager;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.PlayerData;
import com.inferris.player.manager.ManagerContainer;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.server.ErrorCode;
import com.inferris.util.ChatUtil;
import com.inferris.webhook.WebhookBuilder;
import com.inferris.webhook.WebhookType;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandNuke extends Command {
    private final PlayerDataService playerDataService;
    private static final String CONSOLE_IDENTIFIER = "console";
    private final Cache<String, Boolean> confirmationCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES) // Set TTL to 2 minutes
            .build();
    private final ManagerContainer managerContainer;

    @Inject
    public CommandNuke(String name, PlayerDataService playerDataService, ManagerContainer managerContainer) {
        super("nuke");
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

        if (length == 0 || length > 1) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatUtil.translateToHex(
                    """
                            #ff1100Warning! The /nuke command will irreversibly delete an account's data from the database and the Redis keystore. This action cannot be undone.
                            Before proceeding, you MUST request and receive explicit permission.
                            """
            )));
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.RESET + "Usage: /nuclear <account>"));
            return;
        }

        String senderIdentifier = (sender instanceof ProxiedPlayer) ? ((ProxiedPlayer) sender).getUniqueId().toString() : CONSOLE_IDENTIFIER;

        UUID uuid;
        PlayerData playerData; //todo

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

        String cacheKey = senderIdentifier + ":" + playerData.getUuid();
        if (confirmationCache.getIfPresent(cacheKey) == null) {
            confirmationCache.put(cacheKey, true);
            sender.sendMessage(new TextComponent(ChatColor.YELLOW + "Please re-enter the command to confirm the deletion of the player's data. This request will expire in 1 minute."));
            return;
        }

        PlayerContext targetPlayerContext = new PlayerContext(uuid, playerDataService);
        WebhookBuilder webhookBuilder = new WebhookBuilder(WebhookType.LOGS);
        webhookBuilder.setColor(ColorType.DANGER.getColor());
        webhookBuilder.setTitle("Backend Notification");

        if (sender instanceof ProxiedPlayer player) {
            PlayerContext senderPlayerContext = new PlayerContext(player.getUniqueId(), playerDataService);

            channelManager.sendStaffChatMessage(Channel.STAFF, senderPlayerContext.getRank().getByBranch().getPrefix(true)
                    + ChatColor.RESET + player.getName() + ChatColor.YELLOW + " completely erased " + targetPlayerContext.getRank().getByBranch().getPrefix(true)
                    + ChatColor.RESET + playerData.getUsername() + ChatColor.YELLOW + "'s data", ChannelManager.StaffChatMessageType.NOTIFICATION);

            webhookBuilder.setDescription(senderPlayerContext.getRank().getByBranchFormatted(true) + senderPlayerContext.getUsername()
                    + " completely erased " + targetPlayerContext.getRank().getByBranchFormatted(true) + targetPlayerContext.getUsername() + "'s data");
        } else {
            channelManager.sendStaffChatMessage(Channel.STAFF, ChatColor.RED + sender.getName() + ChatColor.YELLOW + " completely erased "
                    + targetPlayerContext.getRank().getByBranch().getPrefix(true)
                    + ChatColor.RESET + playerData.getUsername() + ChatColor.YELLOW + "'s data", ChannelManager.StaffChatMessageType.NOTIFICATION);

            webhookBuilder.setDescription("CONSOLE" + " completely erased "
                    + targetPlayerContext.getRank().getByBranchFormatted(true) + targetPlayerContext.getUsername() + "'s data");
        }

        playerDataService.nukePlayerData(uuid);
        sender.sendMessage(new TextComponent(ChatColor.GREEN + "Player data has been successfully deleted."));
        webhookBuilder.build().sendEmbed();

        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(uuid);
        if (target != null) {
            if (ProxyServer.getInstance().getPlayer(uuid).isConnected()) {
                TextComponent textComponent = new TextComponent(ChatColor.GRAY + "Woa! Something went wrong: " + ErrorCode.PLAYER_DATA_DELETED_BY_ADMIN.getCode(true)
                        + "\n\n" + ErrorCode.PLAYER_DATA_DELETED_BY_ADMIN.getMessage(true) + "\n\n"
                        + ChatColor.WHITE + "If this was unexpected or you need assistance, please reach out to our team, and we'll help resolve the issue!");
                target.disconnect(textComponent);
            }
        }
    }
}
