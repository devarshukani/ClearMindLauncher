package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;

import com.devarshukani.clearmindlauncher.Utils.HomeWatcher;
import com.devarshukani.clearmindlauncher.R;
import com.devarshukani.clearmindlauncher.Fragment.SwipeFragment;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (!isMyLauncherDefault()) {
            promptSetLauncher();
        }

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




}