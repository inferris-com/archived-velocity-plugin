package com.inferris.server.jedis;

public enum JedisChannels {
    GENERIC_FLEX_EVENT("flex_event:generic"),
    PLAYER_FLEX_EVENT("flex_event:player"),
    PLAYERDATA_UPDATE("playerdata:update"),
    PLAYERDATA_RANK_UPDATE("playerdata:rank_update"),
    PLAYERDATA_EVENT_JOIN("playerdata:event_join"),
    PLAYERDATA_VANISH("playerdata:vanish"),
    PROXY_TO_SPIGOT_PLAYERDATA_CACHE_UPDATE("playerdata:cache_update:proxy-to-spigot"),
    SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE("playerdata:cache_update:spigot-to-proxy"),
    VIEW_LOGS_PROXY_TO_SPIGOT("view_logs:proxy-to-spigot"),
    VIEW_LOGS_SPIGOT_TO_PROXY("view_logs:spigot-to-proxy"),
    STAFFCHAT("channel:staff"),

    TEST("test");

    private final String channelName;
    JedisChannels(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }
}
