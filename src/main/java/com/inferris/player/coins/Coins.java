package com.inferris.player.coins;

public class Coins {
    private int balance;

    public Coins(int balance){
        this.balance = balance;
    }

    public Coins(){
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
