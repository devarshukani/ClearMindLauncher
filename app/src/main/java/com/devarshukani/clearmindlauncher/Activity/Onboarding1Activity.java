package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.devarshukani.clearmindlauncher.Activity.SettingsActivity;
import com.devarshukani.clearmindlauncher.R;

public class Onboarding1Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding1);

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }
}