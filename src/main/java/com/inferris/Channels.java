package com.inferris;

public enum Channels {
    STAFF("STAFF"),
    SPECIAL("SPECIAL"),
    NONE("NONE");

    private final String message;
    Channels(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
