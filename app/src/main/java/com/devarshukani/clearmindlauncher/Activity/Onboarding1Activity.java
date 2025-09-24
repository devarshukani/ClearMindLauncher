package com.devarshukani.clearmindlauncher.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;

import com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper;
import com.devarshukani.clearmindlauncher.R;

public class Onboarding1Activity extends AppCompatActivity {

    private static final String FIRST_TIME_KEY = "first_time";

    LinearLayout buttonNext1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_onboarding1);

        // Handle window insets for safe areas
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply padding to avoid status bar and navigation bar overlap
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });

        buttonNext1 = findViewById(R.id.buttonNext1);

        // Check if it's the first time launch
        boolean isFirstTime = (boolean) SharedPreferencesHelper.getData(this, FIRST_TIME_KEY, true);

        if (!isFirstTime) {

            Intent intent = new Intent(this, LauncherActivity.class); // Replace LauncherClass with your actual launcher class
            startActivity(intent);
            finish();

        }

        buttonNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Onboarding2Activity.class);
                startActivity(intent);
            }
        });

    }
}
