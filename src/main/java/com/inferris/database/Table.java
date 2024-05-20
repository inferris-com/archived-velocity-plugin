package com.inferris.database;

public enum Table {
    PLAYER_DATA,
    PROFILE,
    RANK,
    REDEEM,
    VERIFICATION,
    VERIFICATION_SESSIONS;

    public String getName(){
        return this.name().toLowerCase();
    }
}
