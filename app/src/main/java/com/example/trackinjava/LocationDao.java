package com.example.trackinjava;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface LocationDao {
    @Insert
    void insert(LocationEntity location);

    @Query("SELECT * FROM LocationEntity ORDER BY timestamp DESC")
    List<LocationEntity> getAll();

    @Query("SELECT * FROM LocationEntity WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    List<LocationEntity> getBySession(long sessionId);
}

