package com.android.fillmyteam.model;

import java.io.Serializable;

/**
 * User class
 */
public class User implements Serializable {

    private String name;
    private String email;
    private double latitude;
    private double longitude;
    private String sport;
    private String playingTime;
    private String photoUrl;
    private String playingPlace;
    private String playingDate;

    public User() {

    }

    public User(String name, String email, String sport, double latitude, double longitude, String playingTime, String photoUrl, String playingPlace) {
        this.name = name;
        this.email = email;
        this.latitude = latitude;
        this.sport = sport;
        this.longitude = longitude;

        this.playingTime = playingTime;
        this.photoUrl = photoUrl;
        this.playingPlace = playingPlace;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getEmail() {
        return email;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getSport() {
        return sport;
    }


    public String getPlayingTime() {
        return playingTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }


    public void setPlayingTime(String playingTime) {
        this.playingTime = playingTime;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPlayingPlace() {
        return playingPlace;
    }

    public void setPlayingPlace(String playingPlace) {
        this.playingPlace = playingPlace;
    }

    public String getPlayingDate() {
        return playingDate;
    }

    public void setPlayingDate(String playingDate) {
        this.playingDate = playingDate;
    }
}
