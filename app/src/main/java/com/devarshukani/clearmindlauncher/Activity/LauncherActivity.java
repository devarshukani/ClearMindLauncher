package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AppOpsManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.devarshukani.clearmindlauncher.Service.AppBlockerService;
import com.devarshukani.clearmindlauncher.Utils.HomeWatcher;
import com.devarshukani.clearmindlauncher.R;
import com.devarshukani.clearmindlauncher.Fragment.SwipeFragment;

public class LauncherActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;
    private static final int USAGE_STATS_PERMISSION_REQUEST_CODE = 1235;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (!isMyLauncherDefault()) {
            promptSetLauncher();
        }

        // Check and request all necessary permissions, then start the blocker service
        checkAllPermissionsAndStartService();

        HomeWatcher mHomeWatcher = new HomeWatcher(this);
        mHomeWatcher.setOnHomePressedListener(new HomeWatcher.OnHomePressedListener() {
            @Override
            public void onHomePressed() {
                onBackPressed();
            }
            @Override
            public void onHomeLongPressed() {
                onBackPressed();
            }
        });
        mHomeWatcher.startWatch();

        setWallpaper();
    }

    private void checkAllPermissionsAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // Request overlay permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
            } else {
                startAppBlockerService();
            }
        } else {
            startAppBlockerService();
        }

        // Check and request usage stats permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!hasUsageStatsPermission()) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivityForResult(intent, USAGE_STATS_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void startAppBlockerService() {
        Intent serviceIntent = new Intent(this, AppBlockerService.class);
        startService(serviceIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    startAppBlockerService();
                }
            }
        } else if (requestCode == USAGE_STATS_PERMISSION_REQUEST_CODE) {
            // Handle the result for usage stats permission if needed
        }
    }

    private boolean isMyLauncherDefault() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setPackage(getPackageName());
        return intent.resolveActivity(getPackageManager()) != null;
    }

    private void promptSetLauncher() {
        Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Communicate with SwipeFragment to switch to HomeFragment
        SwipeFragment swipeFragment = (SwipeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container); // Replace with your fragment container ID
        if (swipeFragment != null) {
            swipeFragment.switchToHomeFragment();
        } else {
            super.onBackPressed();
        }
    }

    private void setWallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

        try {
            // Create a black bitmap
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            Bitmap blackBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            blackBitmap.eraseColor(Color.BLACK);

            // Set the black bitmap as the wallpaper
            wallpaperManager.setBitmap(blackBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}