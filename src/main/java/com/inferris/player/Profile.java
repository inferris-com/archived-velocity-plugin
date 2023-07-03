package com.inferris.player;

import net.md_5.bungee.api.ChatColor;

import java.time.LocalDate;

public class Profile {
    private String bio;
    private String pronouns;
    private LocalDate registrationDate;
    public Profile(String bio, String pronouns, LocalDate registrationDate){
        this.bio = bio;
        this.pronouns = pronouns;
        this.registrationDate = registrationDate;
    }

    public Profile(){
    }

    public String getBio() {
        return bio;
    }

    public String getPronouns() {
        return pronouns;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
}
