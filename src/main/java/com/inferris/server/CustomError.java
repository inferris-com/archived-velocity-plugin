package com.inferris.server;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class CustomError {
    private final ErrorCode errorCode;
    public CustomError(ErrorCode errorCode){
        this.errorCode = errorCode;
    }

    public TextComponent getError(){
        return new TextComponent(ChatColor.GRAY + "Woa! Something went wrong: " + errorCode.getCode(true)
                + "\n\n" + errorCode.getMessage(true) + "\n\n"
                + ChatColor.WHITE + "If this was unexpected or you need assistance, please reach out to our team, and we'll help resolve the issue!");
    }

    public TextComponent getErrorHeader(){
        return new TextComponent(ChatColor.GRAY + "Woa! Something went wrong: " + errorCode.getCode(true));
    }

    public TextComponent getFooter(){
        return new TextComponent(ChatColor.WHITE + "If this was unexpected or you need assistance, please reach out to our team, and we'll help resolve the issue!");
    }
}
