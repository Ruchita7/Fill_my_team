package com.android.fillmyteam.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.fillmyteam.SportsStoreLocatorFragment;

/**
 * Created by dgnc on 5/29/2016.
 */
public class StoreLocatorParcelable implements Parcelable {
    String name;
    String address;
    double latitude;
    double longitude;


    private StoreLocatorParcelable(Parcel in) {
        name = in.readString();
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public StoreLocatorParcelable(String name, String address, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

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

    public static final Parcelable.Creator<StoreLocatorParcelable> CREATOR
            = new Parcelable.Creator<StoreLocatorParcelable>() {
        public StoreLocatorParcelable createFromParcel(Parcel in) {
            return new StoreLocatorParcelable(in);
        }

        public StoreLocatorParcelable[] newArray(int size) {
            return new StoreLocatorParcelable[size];
        }
    };
}
