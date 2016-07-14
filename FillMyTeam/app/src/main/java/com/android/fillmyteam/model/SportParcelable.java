package com.android.fillmyteam.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Sports Parcelable reference class
 */
public class SportParcelable implements Parcelable {

    String id;
    String sportsName;
    String objective;
    String players;
    String rules;
    String thumbnail;
    String posterImage;
    String videoUrl;

    private SportParcelable(Parcel in)  {
        id=in.readString();
        sportsName=in.readString();
        objective=in.readString();
        players=in.readString();
        rules=in.readString();
        thumbnail=in.readString();
        posterImage=in.readString();
        videoUrl=in.readString();
    }
    public SportParcelable(String id, String sportsName, String objective, String players, String rules, String thumbnail, String posterImage, String videoUrl) {
        this.id = id;
        this.sportsName = sportsName;
        this.objective = objective;
        this.players = players;
        this.rules = rules;
        this.thumbnail = thumbnail;
        this.posterImage = posterImage;
        this.videoUrl = videoUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(sportsName);
        dest.writeString(objective);
        dest.writeString(players);
        dest.writeString(rules);
        dest.writeString(thumbnail);
        dest.writeString(posterImage);
        dest.writeString(videoUrl);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSportsName() {
        return sportsName;
    }

    public void setSportsName(String sportsName) {
        this.sportsName = sportsName;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getPosterImage() {
        return posterImage;
    }

    public void setPosterImage(String posterImage) {
        this.posterImage = posterImage;
    }

    public String getPlayers() {
        return players;
    }

    public void setPlayers(String players) {
        this.players = players;
    }

    public static final Parcelable.Creator<SportParcelable> CREATOR
            = new Parcelable.Creator<SportParcelable>() {
        public SportParcelable createFromParcel(Parcel in) {
            return new SportParcelable(in);
        }

        public SportParcelable[] newArray(int size) {
            return new SportParcelable[size];
        }
    };
}
