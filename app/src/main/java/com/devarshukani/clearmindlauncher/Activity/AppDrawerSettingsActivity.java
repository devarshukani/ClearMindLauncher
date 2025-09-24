package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.devarshukani.clearmindlauncher.R;
import com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper;
import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class AppDrawerSettingsActivity extends AppCompatActivity {

    MaterialSwitch switchAlwaysShowKeyboard;
    MaterialSwitch switchAutoStartApp;
    MaterialSwitch switchShowAppIcons;
    MaterialSwitch switchQuickSearch; // Add Quick Search toggle

    private AnimateLinearLayoutButton animHelper; // Add haptics helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_drawer_settings);

        animHelper = new AnimateLinearLayoutButton(); // Initialize haptics helper

        switchAlwaysShowKeyboard = findViewById(R.id.switchAlwaysShowKeyboard);
        switchAutoStartApp = findViewById(R.id.switchAutoStartApp);
        switchShowAppIcons = findViewById(R.id.switchShowAppIcons);
        switchQuickSearch = findViewById(R.id.switchQuickSearch); // Initialize Quick Search toggle

        // Retrieve Saved Preferences
        boolean alwaysShowKeyboard = (boolean) SharedPreferencesHelper.getData(this, "AppDrawerAlwaysShowKeyboard", false);
        switchAlwaysShowKeyboard.setChecked(alwaysShowKeyboard);

        boolean autoStartApp = (boolean) SharedPreferencesHelper.getData(this, "AppDrawerAutoStartApp", true);
        switchAutoStartApp.setChecked(autoStartApp);

        boolean showAppIcons = (boolean) SharedPreferencesHelper.getData(this, "AppDrawerShowAppIcons", false);
        switchShowAppIcons.setChecked(showAppIcons);

        boolean quickSearch = (boolean) SharedPreferencesHelper.getData(this, "AppDrawerQuickSearch", false);
        switchQuickSearch.setChecked(quickSearch);

        // Update The Preferences on Change with haptic feedback
        switchAlwaysShowKeyboard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                animHelper.animateButtonClickWithHaptics(buttonView); // Add haptic feedback
                // Update the preference when the switch state changes
                SharedPreferencesHelper.saveData(AppDrawerSettingsActivity.this, "AppDrawerAlwaysShowKeyboard", isChecked);
            }
        });

        switchAutoStartApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                animHelper.animateButtonClickWithHaptics(buttonView); // Add haptic feedback

                if (isChecked) {
                    // If Auto Start App is enabled, disable Quick Search
                    if (switchQuickSearch.isChecked()) {
                        switchQuickSearch.setChecked(false);
                        SharedPreferencesHelper.saveData(AppDrawerSettingsActivity.this, "AppDrawerQuickSearch", false);
                    }
                }

                SharedPreferencesHelper.saveData(AppDrawerSettingsActivity.this, "AppDrawerAutoStartApp", isChecked);
            }
        });

        switchShowAppIcons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                animHelper.animateButtonClickWithHaptics(buttonView); // Add haptic feedback
                // Update the preference when the switch state changes
                SharedPreferencesHelper.saveData(AppDrawerSettingsActivity.this, "AppDrawerShowAppIcons", isChecked);
            }
        });

        switchQuickSearch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                animHelper.animateButtonClickWithHaptics(buttonView); // Add haptic feedback

                if (isChecked) {
                    // If Quick Search is enabled, disable Auto Start App
                    if (switchAutoStartApp.isChecked()) {
                        switchAutoStartApp.setChecked(false);
                        SharedPreferencesHelper.saveData(AppDrawerSettingsActivity.this, "AppDrawerAutoStartApp", false);
                    }
                }

                SharedPreferencesHelper.saveData(AppDrawerSettingsActivity.this, "AppDrawerQuickSearch", isChecked);
            }
        });

    }
}