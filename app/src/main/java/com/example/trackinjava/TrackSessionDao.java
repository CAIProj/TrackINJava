package com.example.trackinjava;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TrackSessionDao {
    @Insert
    long insert(TrackSession session); // returns new session ID

    @Query("SELECT * FROM TrackSession ORDER BY startTime DESC")
    List<TrackSession> getAll();

    @Query("UPDATE TrackSession SET endTime = :end WHERE sessionId = :id")
    void endSession(long id, long end);
}
