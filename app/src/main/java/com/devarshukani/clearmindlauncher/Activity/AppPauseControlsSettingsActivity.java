package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

import com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper;
import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class AppPauseControlsSettingsActivity extends AppCompatActivity {

    MaterialSwitch switchTemporaryAccess;
    private AnimateLinearLayoutButton animHelper; // Add haptics helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_app_pause_controls_settings);

        // Handle window insets for safe areas
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply padding to avoid status bar and navigation bar overlap
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });

        animHelper = new AnimateLinearLayoutButton(); // Initialize haptics helper

        switchTemporaryAccess = findViewById(R.id.switchTemporaryAccess);


        boolean temporaryAccess = (boolean) SharedPreferencesHelper.getData(this, "AppPauseControlsTemporaryAccess", false);
        switchTemporaryAccess.setChecked(temporaryAccess);

        switchTemporaryAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                animHelper.animateButtonClickWithHaptics(compoundButton); // Add haptic feedback
                SharedPreferencesHelper.saveData(AppPauseControlsSettingsActivity.this, "AppPauseControlsTemporaryAccess", isChecked);
            }
        });

    }
}