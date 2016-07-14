package com.android.fillmyteam.model;

/**
 * Model class for keeping track of matches
 */
public class Match {
    double latitude;
    double longitude;
    String playerEmail;

    String playingPlace;
    long playingTime;
    String playingWith;
    String sport;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPlayerEmail() {
        return playerEmail;
    }

    public void setPlayerEmail(String playerEmail) {
        this.playerEmail = playerEmail;
    }

    public String getPlayingPlace() {
        return playingPlace;
    }

    public void setPlayingPlace(String playingPlace) {
        this.playingPlace = playingPlace;
    }

    public long getPlayingTime() {
        return playingTime;
    }

    public void setPlayingTime(long playingTime) {
        this.playingTime = playingTime;
    }

    public String getPlayingWith() {
        return playingWith;
    }

    public void setPlayingWith(String playingWith) {
        this.playingWith = playingWith;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }
}
