package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class HomeScreenSettingsActivity extends AppCompatActivity {

    LinearLayout ButtonFavouriteApps;
    LinearLayout ButtonClockFaces;

    private AnimateLinearLayoutButton animHelper; // Add haptics helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_home_screen_settings);

        // Handle window insets for safe areas
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply padding to avoid status bar and navigation bar overlap
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });

        animHelper = new AnimateLinearLayoutButton(); // Initialize haptics helper

        ButtonFavouriteApps = findViewById(R.id.ButtonFavouriteApps);
        ButtonClockFaces = findViewById(R.id.ButtonClockFaces);

        ButtonFavouriteApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonFavouriteApps); // Add haptic feedback

                Intent intent = new Intent(HomeScreenSettingsActivity.this, FavouriteAppsSettingsActivity.class);
                startActivity(intent);
            }
        });

        ButtonClockFaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonClockFaces); // Add haptic feedback

                Intent intent = new Intent(HomeScreenSettingsActivity.this, ClockFaceSettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}