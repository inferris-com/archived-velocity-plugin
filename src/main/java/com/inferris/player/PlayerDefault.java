package com.inferris.player;

public enum PlayerDefault {
    COIN_BALANCE(50);

    private final int value;
    PlayerDefault(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
