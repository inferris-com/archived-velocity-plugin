package com.inferris.server;

public enum ErrorCode {
    COMMAND_ERROR(100, "Command error"),
    EVENT_ERROR(101, "Event error"),
    DATABASE_ERROR(102, "Database error"),
    REDIS_ERROR(103, "Jedis error"),
    PROXY_STOPPED_BY_ADMIN(200, "Proxy has been stopped by an administrator."),
    NETWORK_KILLED(201, "Network has been shutdown; all instances killed. Probably applying a patch or hotfix."),
    PLAYER_DATA_DELETED_BY_ADMIN(210, "Account data has been deleted by an administrator."),
    INTERNAL_LOCKDOWN(211, "Internal lockdown");

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
}
