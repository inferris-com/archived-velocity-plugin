package com.inferris;

public enum Subchannel {
    REQUEST,
    RESPONSE,
    FORWARD;

    private final String lowercaseName;

    Subchannel(){
        this.lowercaseName = name().toLowerCase();
    }

    public String toLowerCase() {
        return lowercaseName;
    }
}
