package com.inferris.server;

import net.md_5.bungee.api.ChatColor;

public enum ErrorCode {
    PROXY_STOPPED_BY_ADMIN(200, "Proxy has been stopped by an administrator."),
    PLAYER_DATA_DELETED_BY_ADMIN(201, "Account data has been deleted by an administrator.");

    private final int code;
    private final String message;

    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getCode(boolean formatted) {
        if (formatted) {
            return String.format("ERR-%03d", code); // Example formatting, e.g., ERR-100
        }
        return String.valueOf(code);
    }

    public String getMessage() {
        return message;
    }

    public String getMessage(boolean formatted) {
        if(formatted){
            return ChatColor.RED + message;
        }
        return message;
    }
}
