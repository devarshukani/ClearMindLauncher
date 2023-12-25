package com.devarshukani.clearmindlauncher.Database;

import androidx.core.app.NotificationCompat;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Entity(tableName = "PausedAppsDetails")
public class PausedApps implements Serializable {

    @PrimaryKey
    @NotNull
    @ColumnInfo(name = "packageName")
    String packageName;


    @ColumnInfo(name = "pausedStartTime")
    String pausedStartTime;

    @ColumnInfo(name = "pausedEndTime")
    String pausedEndTime;


    @NotNull
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(@NotNull String packageName) {
        this.packageName = packageName;
    }

    public String getPausedStartTime() {
        return pausedStartTime;
    }

    public void setPausedStartTime(String pausedStartTime) {
        this.pausedStartTime = pausedStartTime;
    }

    public String getPausedEndTime() {
        return pausedEndTime;
    }

    public void setPausedEndTime(String pausedEndTime) {
        this.pausedEndTime = pausedEndTime;
    }
}