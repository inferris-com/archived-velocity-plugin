package com.inferris.server;

public enum JedisChannels {
    PLAYERDATA_UPDATE("playerdata:update"),
    PLAYERDATA_JOIN("playerdata:join"),
    PLAYERDATA_VANISH("playerdata:vanish"),
    PROXY_TO_SPIGOT_PLAYERDATA_CACHE_UPDATE("playerdata:cache_update:proxy-to-spigot"),
    SPIGOT_TO_PROXY_PLAYERDATA_CACHE_UPDATE("playerdata:cache_update:spigot-to-proxy"),
    VIEW_LOGS_PROXY_TO_SPIGOT("view_logs:proxy-to-spigot"),
    VIEW_LOGS_SPIGOT_TO_PROXY("view_logs:spigot-to-proxy"),
    TEST("test");

    private final String channelName;
    JedisChannels(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelName() {
        return channelName;
    }
}
