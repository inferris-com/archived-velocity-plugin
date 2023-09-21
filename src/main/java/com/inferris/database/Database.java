package com.inferris.database;

public enum Database {
    INFERRIS,
    XENFORO;

    public String getType(){
        return this.name().toLowerCase();
    }
}
