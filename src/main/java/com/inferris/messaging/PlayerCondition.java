package com.inferris.messaging;

import net.md_5.bungee.api.connection.ProxiedPlayer;

@FunctionalInterface
public interface PlayerCondition {
    boolean shouldSendMessage(ProxiedPlayer player);
}
