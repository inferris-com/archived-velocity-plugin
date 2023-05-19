package com.inferris.server;

public enum Subchannel {
    REQUEST,
    RESPONSE,
    FORWARD,

    VANISH;

    private final String lowercaseName;

    Subchannel(){
        this.lowercaseName = name().toLowerCase();
    }

    public String toLowerCase() {
        return lowercaseName;
    }
}
