package com.sample.android.fillmyteam.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Sports Parcelable reference class
 */
public class SportParcelable implements Parcelable {

    String id;
 //   String sportsName;
    String name;
    String objective;
    String players;
    String rules;
    String thumbnail;
  //  String posterImage;
    String image;
 //   String videoUrl;
    String video_reference;

    private SportParcelable(Parcel in)  {
        id=in.readString();
        name=in.readString();
        objective=in.readString();
        players=in.readString();
        rules=in.readString();
        thumbnail=in.readString();
        image=in.readString();
        video_reference=in.readString();
    }
    public SportParcelable(String id, String sportsName, String objective, String players, String rules, String thumbnail, String posterImage, String videoUrl) {
        this.id = id;
        this.name = sportsName;
        this.objective = objective;
        this.players = players;
        this.rules = rules;
        this.thumbnail = thumbnail;
        this.image = posterImage;
        this.video_reference = videoUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(objective);
        dest.writeString(players);
        dest.writeString(rules);
        dest.writeString(thumbnail);
        dest.writeString(image);
        dest.writeString(video_reference);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideo_reference() {
        return video_reference;
    }

    public void setVideo_reference(String video_reference) {
        this.video_reference = video_reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
