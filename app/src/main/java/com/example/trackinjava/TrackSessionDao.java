package com.example.trackinjava;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TrackSessionDao {
    @Insert
    long insert(TrackSessionEntity session); // returns new session ID

    @Query("SELECT * FROM TrackSessionEntity ORDER BY startTime DESC")
    List<TrackSessionEntity> getAll();

    @Query("UPDATE TrackSessionEntity SET endTime = :end WHERE sessionId = :id")
    void endSession(long id, long end);
}
