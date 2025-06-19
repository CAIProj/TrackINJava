package com.example.trackinjava;

import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LocationEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long sessionId;
    public double altitude;
    public double latitude;
    public double longitude;
    public long timestamp;

    public LocationEntity(long sessionId, double altitude, double latitude, double longitude, long timestamp) {
        this.sessionId = sessionId;
        this.altitude = altitude;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("Session id: " + sessionId +
                "\nAltitude: " + altitude +
                "\nLatitude: " + latitude +
                "\nLongitude: " + longitude +
                "\nTimestamp : " + convertDate(timestamp));
    }

    private static String convertDate(long dateInMilliseconds) {
        return DateFormat.format("dd/MM/yyyy hh:mm:ss", dateInMilliseconds).toString();
    }
}