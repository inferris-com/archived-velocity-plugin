package com.inferris.commands;

import com.google.inject.Inject;
import com.inferris.Inferris;
import com.inferris.events.redis.EventPayload;
import com.inferris.events.redis.GenericAction;
import com.inferris.player.context.PlayerContext;
import com.inferris.player.service.PlayerDataService;
import com.inferris.rank.Branch;
import com.inferris.server.jedis.JedisChannel;
import com.inferris.server.jedis.JedisHelper;
import com.inferris.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandShout extends Command {
    private final PlayerDataService playerDataService;

    @Inject
    public CommandShout(String name, PlayerDataService playerDataService) {
        super(name);
        this.playerDataService = playerDataService;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        int length = args.length;
        StringBuilder message = new StringBuilder();
        if(length > 0) {
            for (String word : args) {
                message.append(word).append(" "); // Add a space between words
            }

            Inferris.getInstance().getLogger().info("Shout: " + message);
            JedisHelper.publish(JedisChannel.GENERIC_FLEX_EVENT, new EventPayload(GenericAction.ALERT, "ENTITY_CHICKEN_EGG", Inferris.getInstanceId()));

            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                player.sendMessage(ChatUtil.translateToHex(message.toString()));
            }
        }else{
            sender.sendMessage(new TextComponent(ChatColor.RED + "Usage error: You must provide text for the command"));
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (sender instanceof ProxiedPlayer player) {
            PlayerContext playerContext = new PlayerContext(player.getUniqueId(), playerDataService);
            return playerContext.getRank().getBranchValue(Branch.STAFF) >= 3;
        }
        return true;
    }
}