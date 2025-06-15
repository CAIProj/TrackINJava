package com.example.trackinjava;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TrackSession {
    @PrimaryKey(autoGenerate = true)
    public long sessionId;

    public long startTime;
    public long endTime;
}
