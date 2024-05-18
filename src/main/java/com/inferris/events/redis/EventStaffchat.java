package com.inferris.events.redis;

import com.inferris.Inferris;
import com.inferris.events.redis.dispatching.JedisEventHandler;
import com.inferris.messaging.StaffChatMessage;
import com.inferris.player.PlayerData;
import com.inferris.player.PlayerDataManager;
import com.inferris.rank.Branch;
import com.inferris.rank.RankRegistry;
import com.inferris.serialization.StaffChatSerializer;
import com.inferris.server.ServerState;
import com.inferris.server.ServerStateManager;
import com.inferris.server.Tags;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class EventStaffchat implements JedisEventHandler {
    @Override
    public void handle(String message) {

        if (ServerStateManager.getCurrentState() == ServerState.DEBUG)
            Inferris.getInstance().getLogger().severe("Triggered");

        StaffChatMessage staffChatMessage = StaffChatSerializer.deserialize(message);
        assert staffChatMessage != null;

        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(staffChatMessage.getPlayerUUID());
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerData.getUuid());
        ProxyServer proxyServer = ProxyServer.getInstance();
        RankRegistry rank = playerData.getByBranch();

        BaseComponent[] textComponent = TextComponent.fromLegacyText(Tags.STAFF.getName(true)
                + rank.getPrefix(true) + playerData.getNameColor() + player.getName() + ChatColor.RESET + ": " + staffChatMessage.getMessage());

        for (ProxiedPlayer proxiedPlayers : proxyServer.getPlayers()) {
            if (PlayerDataManager.getInstance().getPlayerData(proxiedPlayers).getBranchValue(Branch.STAFF) >= 1) {
                proxiedPlayers.sendMessage(textComponent);
            }
        }
    }
}
