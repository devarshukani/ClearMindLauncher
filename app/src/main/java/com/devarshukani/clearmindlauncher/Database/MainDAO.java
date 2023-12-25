package com.devarshukani.clearmindlauncher.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomDatabase;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MainDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PausedApps pausedApps);

    @Query("UPDATE PausedAppsDetails SET pausedStartTime = :pausedStartTime, pausedEndTime = :pausedEndTime WHERE packageName = :packageName")
    void update(String packageName, String  pausedStartTime, String pausedEndTime);

    @Delete
    void delete(PausedApps pausedApps);

    @Query("SELECT * FROM PausedAppsDetails")
    List<PausedApps> getAll();

    @Query("SELECT * FROM PausedAppsDetails WHERE packageName = :packageName")
    PausedApps getSingleApp(String packageName);
}