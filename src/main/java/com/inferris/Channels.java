package com.inferris;

public enum Channels {
    STAFF("STAFF"),
    SPECIAL("SPECIAL"),
    NONE("NONE");

    private final String message;
    private final String lowercaseName;
    Channels(String message){
        this.message = message;
        this.lowercaseName = name().toLowerCase();
    }

    public String getMessage() {
        return message;
    }

    public String getLowercaseName() {
        return lowercaseName;
    }
}
