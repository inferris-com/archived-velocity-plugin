package com.inferris.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Profile {
    @JsonProperty("registrationDate")
    private Long registrationDate;
    @JsonProperty("bio")
    private String bio;
    @JsonProperty("pronouns")
    private String pronouns;
    @JsonProperty("xenforoId")
    private int xenforoId;
    @JsonProperty("discordLinked")
    private boolean discordLinked;
    @JsonProperty("isFlagged")
    private boolean isFlagged;

    public Profile(Long registrationDate, String bio, String pronouns, int xenforoId, boolean discordLinked, boolean isFlagged) {
        this.registrationDate = registrationDate;
        this.bio = bio;
        this.pronouns = pronouns;
        this.xenforoId = xenforoId;
        this.discordLinked = discordLinked;
        this.isFlagged = isFlagged;
    }

    public Profile() {
    }

    // Method to get the Unix timestamp of the registration date
    public Long getRegistrationDate() {
        return (registrationDate == null) ? Instant.now().getEpochSecond() : registrationDate;
    }

    // Method to get the formatted registration date with a custom pattern
    public String getFormattedRegistrationDate(String pattern) {
        Instant instant = (registrationDate == null) ? Instant.now() : Instant.ofEpochSecond(registrationDate);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));
        return zonedDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    // Method to get only the date part in the default format (yyyy-MM-dd)
    public String getRegistrationDateOnly() {
        return getFormattedRegistrationDate("yyyy-MM-dd");
    }

    // Method to get only the time part in the default format (HH:mm)
    public String getRegistrationTimeOnly() {
        return getFormattedRegistrationDate("HH:mm z");
    }

    // Method to get both date and time in the default format (yyyy-MM-dd HH:mm z)
    public String getRegistrationDateTime() {
        return getFormattedRegistrationDate("yyyy-MM-dd HH:mm z");
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

    public void setRegistrationDate(Long registrationDate) {
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

    public boolean isFlagged() {
        return isFlagged;
    }

    @JsonIgnore
    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }
}
