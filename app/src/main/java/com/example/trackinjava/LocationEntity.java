package com.example.trackinjava;

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
}