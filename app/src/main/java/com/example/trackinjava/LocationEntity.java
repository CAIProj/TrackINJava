package com.example.trackinjava;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LocationEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public double latitude;
    public double longitude;
    public long timestamp;

    public LocationEntity(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }
}
