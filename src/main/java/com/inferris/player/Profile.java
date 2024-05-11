package com.inferris.player;

import java.time.LocalDate;

public class Profile {
    private LocalDate registrationDate;
    private String bio;
    private String pronouns;
    private int xenforoId;
    private boolean discordLinked;
    public Profile(LocalDate registrationDate, String bio, String pronouns, int xenforoId, boolean discordLinked){
        this.registrationDate = registrationDate;
        this.bio = bio;
        this.pronouns = pronouns;
        this.xenforoId = xenforoId;
        this.discordLinked = discordLinked;
    }

    public Profile(){
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    public String getBio() {
        return bio;
    }

    public String getPronouns() {
        return pronouns;
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

    public int getXenforoId() {
        return xenforoId;
    }

    public void setXenforoId(int xenforoId) {
        this.xenforoId = xenforoId;
    }

    public boolean isDiscordLinked() {
        return discordLinked;
    }

    public void setDiscordLinked(boolean discordLinked) {
        this.discordLinked = discordLinked;
    }
}
