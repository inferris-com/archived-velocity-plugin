package com.inferris.server;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class CustomError {
    private final String HEADER;
    private final String DESCRIPTION;
    private final String FOOTER;

    public CustomError(ErrorCode errorCode){
        this.HEADER = ChatColor.GRAY + "Woa! Something went wrong: " + errorCode.getCode(true);
        this.DESCRIPTION = ChatColor.RED + errorCode.getMessage();
        this.FOOTER = ChatColor.WHITE + "If this was unexpected or you need assistance, please reach out to our team, and we'll help resolve the issue!";
    }

    public TextComponent getTemplate() {
        String template = String.format("%s%n%n%s%n%n%s", HEADER, DESCRIPTION, FOOTER);
        return new TextComponent(template);
    }

    public TextComponent getTemplate(String customDetails){
        String template = String.format("%s%n%n%s%n%n%s", HEADER, DESCRIPTION, ChatColor.RESET + customDetails);
        return new TextComponent(template);
    }

    public String getHeader() {
        return HEADER;
    }

    public String getFooter() {
        return FOOTER;
    }
}
