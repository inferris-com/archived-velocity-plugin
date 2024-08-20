package com.inferris.webhook;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;

public class WebhookBuilder {
    private final WebhookClientBuilder clientBuilder;
    private final WebhookClient client;
    private WebhookEmbed embed;
    private final WebhookEmbedBuilder embedBuilder;

    public WebhookBuilder(WebhookType webhookType) {
        clientBuilder = new WebhookClientBuilder(webhookType.getUrl()); // or id, token
        clientBuilder.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setDaemon(true);
            return thread;
        });
        clientBuilder.setWait(true);
        client = clientBuilder.build();

        embedBuilder = new WebhookEmbedBuilder();
    }

    public WebhookBuilder setColor(Color color) {
        embedBuilder.setColor(color.getRGB());
        return this;
    }

    public WebhookBuilder setTitle(String title) {
        embedBuilder.setTitle(new WebhookEmbed.EmbedTitle(title, null));
        return this;
    }

    public WebhookBuilder setTitle(@NotNull String title, @Nullable String url) {
        embedBuilder.setTitle(new WebhookEmbed.EmbedTitle(title, url));
        return this;
    }

    public WebhookBuilder setDescription(String description) {
        embedBuilder.setDescription(description);
        return this;
    }

    public WebhookBuilder setAuthor(@NotNull String author, @Nullable String iconUrl, @Nullable String url) {
        embedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(author, iconUrl, url));
        return this;
    }

    public WebhookBuilder addField(boolean incline, @NotNull String name, @NotNull String value) {
        embedBuilder.addField(new WebhookEmbed.EmbedField(incline, name, value));
        return this;
    }

    public WebhookBuilder build() {
        embedBuilder.build();
        return this;
    }

    public void sendEmbed() {
        embed = embedBuilder.build();
        client.send(embed);
    }
}
