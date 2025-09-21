package com.devarshukani.clearmindlauncher.Service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.devarshukani.clearmindlauncher.Activity.PausedAppOverlayActivity;
import com.devarshukani.clearmindlauncher.Database.PausedApps;
import com.devarshukani.clearmindlauncher.Database.RoomDB;

import java.util.List;

public class AppBlockerService extends Service {
    
    private static final String TAG = "AppBlockerService";
    private Handler handler;
    private Runnable checkRunnable;
    private RoomDB database;
    private String lastForegroundApp = "";
    private long lastCheckTime;

    @Override
    public void onCreate() {
        super.onCreate();
        database = RoomDB.getInstance(this);
        handler = new Handler(Looper.getMainLooper());
        lastCheckTime = System.currentTimeMillis();
        startMonitoring();
    }

    private void startMonitoring() {
        checkRunnable = new Runnable() {
            @Override
            public void run() {
                checkForegroundApp();
                handler.postDelayed(this, 500); // Check every 500ms for more responsiveness
            }
        };
        handler.post(checkRunnable);
    }

    private void checkForegroundApp() {
        String currentApp = getCurrentForegroundApp();
        if (currentApp != null && 
            !currentApp.equals(getPackageName()) && 
            !currentApp.equals("com.android.systemui") &&
            !currentApp.equals("android") &&
            !currentApp.equals(lastForegroundApp)) {
            
            Log.d(TAG, "App switched to: " + currentApp);
            
            if (isAppPaused(currentApp)) {
                Log.d(TAG, "Paused app detected: " + currentApp);
                showOverlay(currentApp);
            }
            lastForegroundApp = currentApp;
        }
    }

    private String getCurrentForegroundApp() {
        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long currentTime = System.currentTimeMillis();
            
            // Get usage events for the last 2 seconds
            UsageEvents usageEvents = usageStatsManager.queryEvents(currentTime - 2000, currentTime);
            UsageEvents.Event event = new UsageEvents.Event();
            String foregroundApp = null;
            
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    foregroundApp = event.getPackageName();
                }
            }
            
            // Fallback to ActivityManager for older Android versions or if UsageEvents fails
            if (foregroundApp == null) {
                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
                if (!runningTasks.isEmpty()) {
                    foregroundApp = runningTasks.get(0).topActivity.getPackageName();
                }
            }
            
            return foregroundApp;
        } catch (Exception e) {
            Log.e(TAG, "Error getting foreground app: " + e.getMessage());
            return null;
        }
    }

    private boolean isAppPaused(String packageName) {
        List<PausedApps> pausedAppsList = database.mainDAO().getAll();
        long currentTimeMillis = System.currentTimeMillis();
        
        for (PausedApps pausedApp : pausedAppsList) {
            if (packageName.equals(pausedApp.getPackageName())) {
                long startTimeMillis = Long.parseLong(pausedApp.getPausedStartTime());
                long endTimeMillis = Long.parseLong(pausedApp.getPausedEndTime());
                
                if (currentTimeMillis >= startTimeMillis && currentTimeMillis <= endTimeMillis) {
                    return true;
                }
            }
        }
        return false;
    }

    private void showOverlay(String packageName) {
        Intent overlayIntent = new Intent(this, PausedAppOverlayActivity.class);
        overlayIntent.putExtra("blocked_package", packageName);
        overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                             Intent.FLAG_ACTIVITY_CLEAR_TOP | 
                             Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(overlayIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Restart service if killed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && checkRunnable != null) {
            handler.removeCallbacks(checkRunnable);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}