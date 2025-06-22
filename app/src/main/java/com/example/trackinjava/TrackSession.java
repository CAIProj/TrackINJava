package com.example.trackinjava;

import android.text.format.DateFormat;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TrackSession {
    @PrimaryKey(autoGenerate = true)
    public long sessionId;

    public long startTime;
    public long endTime;

    @NonNull
    @Override
    public String toString() {
        return String.format("Session id: " + sessionId +
                            "\nStart: " + convertDate(startTime));
    }

    private static String convertDate(long dateInMilliseconds) {
        return DateFormat.format("dd/MM/yyyy HH:mm:ss", dateInMilliseconds).toString();
    }
}
