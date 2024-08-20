package com.inferris.webhook;

import com.inferris.config.ConfigType;
import com.inferris.config.ConfigurationHandler;

public enum WebhookType {
    LOGS(ConfigurationHandler.getInstance().getConfig(ConfigType.CONFIG).getString("webhooks.logs")),
    STATUS(ConfigurationHandler.getInstance().getConfig(ConfigType.CONFIG).getString("webhooks.status"));

    private final String url;

    WebhookType(String url){
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
