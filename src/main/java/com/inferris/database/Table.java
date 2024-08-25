package com.inferris.database;

public enum Table {
    PLAYER_DATA,
    PROFILE,
    RANK,
    FLAGGED_PLAYERS,
    REDEEM,
    VERIFICATION,
    VERIFICATION_SESSIONS;

    public String getName(){
        return this.name().toLowerCase();
    }
}
