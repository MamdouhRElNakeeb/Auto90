package com.be4em.auto90.object;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by mamdouhelnakeeb on 5/1/17.
 */

public class RideItem implements Parcelable{

    public int rideID;
    public int driverID = 0;
    public String rideName;
    public float rideTime;
    public String rideDays;
    public int ridePrice;
    public int rideNotifs;
    public double latStart, lonStart, latEnd, lonEnd;
    public String subscription;
    public double fare;

    public RideItem (int rideID, int driverID, String rideName, double latStart, double lonStart, double latEnd, double lonEnd, float rideTime, String rideDays){
        this.rideID = rideID;
        this.driverID = driverID;
        this.rideName = rideName;
        this.latStart = latStart;
        this.lonStart = lonStart;
        this.latEnd = latEnd;
        this.lonEnd = lonEnd;
        this.rideTime = rideTime;
        this.rideDays = rideDays;
    }

    public RideItem (int rideID, int driverID, String rideName, double latStart, double lonStart, double latEnd, double lonEnd, float rideTime, String rideDays, String subscription, double fare){
        this.rideID = rideID;
        this.driverID = driverID;
        this.rideName = rideName;
        this.latStart = latStart;
        this.lonStart = lonStart;
        this.latEnd = latEnd;
        this.lonEnd = lonEnd;
        this.rideTime = rideTime;
        this.rideDays = rideDays;
        this.subscription = subscription;
        this.fare = fare;
    }

    protected RideItem(Parcel in) {
        rideID = in.readInt();
        driverID = in.readInt();
        rideName = in.readString();
        rideTime = in.readFloat();
        rideDays = in.readString();
        ridePrice = in.readInt();
        rideNotifs = in.readInt();
        latStart = in.readDouble();
        lonStart = in.readDouble();
        latEnd = in.readDouble();
        lonEnd = in.readDouble();
        subscription = in.readString();
        fare = in.readDouble();
    }

    public static final Creator<RideItem> CREATOR = new Creator<RideItem>() {
        @Override
        public RideItem createFromParcel(Parcel in) {
            return new RideItem(in);
        }

        @Override
        public RideItem[] newArray(int size) {
            return new RideItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(rideID);
        parcel.writeInt(driverID);
        parcel.writeString(rideName);
        parcel.writeFloat(rideTime);
        parcel.writeString(rideDays);
        parcel.writeInt(ridePrice);
        parcel.writeInt(rideNotifs);
        parcel.writeDouble(latStart);
        parcel.writeDouble(lonStart);
        parcel.writeDouble(latEnd);
        parcel.writeDouble(lonEnd);
        parcel.writeString(subscription);
        parcel.writeDouble(fare);
    }
}
