package com.inferris.player;

import java.time.LocalDate;

public class Profile {
    private String bio;
    private String pronouns;
    private LocalDate registrationDate;
    private int xenforoId;
    public Profile(String bio, String pronouns, LocalDate registrationDate, int xenforoId){
        this.bio = bio;
        this.pronouns = pronouns;
        this.registrationDate = registrationDate;
        this.xenforoId = xenforoId;
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

    public int getXenforoId() {
        return xenforoId;
    }

    public void setXenforoId(int xenforoId) {
        this.xenforoId = xenforoId;
    }
}
