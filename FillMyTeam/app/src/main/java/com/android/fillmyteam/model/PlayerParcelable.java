package com.android.fillmyteam.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dgnc on 5/15/2016.
 */
public class PlayerParcelable implements Parcelable{
    User user;

    private PlayerParcelable(Parcel in)  {
        user = (User) in.readSerializable();

    }

    public  PlayerParcelable(User user) {
        this.user=user;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(user);

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static final Parcelable.Creator<PlayerParcelable> CREATOR
            = new Parcelable.Creator<PlayerParcelable>() {
        public PlayerParcelable createFromParcel(Parcel in) {
            return new PlayerParcelable(in);
        }

        public PlayerParcelable[] newArray(int size) {
            return new PlayerParcelable[size];
        }
    };

}
