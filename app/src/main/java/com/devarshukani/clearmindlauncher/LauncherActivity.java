package com.devarshukani.clearmindlauncher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        if (!isMyLauncherDefault()) {
            promptSetLauncher();
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
    public void onBackPressed(){
        // do nothing
    }

}