package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper;
import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.R;

public class Onboarding3Activity extends AppCompatActivity {

    private static final String FIRST_TIME_KEY = "first_time";
    LinearLayout buttonGetStarted;
    private AnimateLinearLayoutButton animHelper; // Add haptics helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding3);

        animHelper = new AnimateLinearLayoutButton(); // Initialize haptics helper

        buttonGetStarted = findViewById(R.id.buttonGetStarted);

        buttonGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(buttonGetStarted); // Add haptic feedback
                SharedPreferencesHelper.saveData(getApplicationContext(), FIRST_TIME_KEY, false);
                Intent intent = new Intent(getApplicationContext(), LauncherActivity.class);
                startActivity(intent);
            }
        });
    }
}