package com.inferris.player;

public enum PlayerDefaults {
    COIN_BALANCE(36);

    private final int value;
    PlayerDefaults(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
