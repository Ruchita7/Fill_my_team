package com.android.fillmyteam.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dgnc on 5/29/2016.
 */
public class StoreLocatorParcelable implements Parcelable {
    String name;
    String address;
    double latitude;
    double longitude;
    String photoReference;

    private StoreLocatorParcelable(Parcel in) {
        name = in.readString();
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        photoReference=in.readString();
    }

    public StoreLocatorParcelable(String name, String address, double latitude, double longitude,String photoReference) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoReference=photoReference;
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
        dest.writeString(photoReference);
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

    public String getPhotoReference() {
        return photoReference;
    }

    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
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
