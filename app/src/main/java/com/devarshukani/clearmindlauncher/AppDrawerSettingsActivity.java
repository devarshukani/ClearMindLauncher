package com.devarshukani.clearmindlauncher;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class AppDrawerSettingsActivity extends AppCompatActivity {


    SwitchMaterial switchAlwaysShowKeyboard;
    SwitchMaterial switchAutoStartApp;
    SwitchMaterial switchShowAppIcons;
    SwitchMaterial switchHidePausedApps;
    Spinner spinnerSearchBarPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_drawer_settings);

        switchAlwaysShowKeyboard = findViewById(R.id.switchAlwaysShowKeyboard);
        switchAutoStartApp = findViewById(R.id.switchAutoStartApp);
        switchShowAppIcons = findViewById(R.id.switchShowAppIcons);
        switchHidePausedApps = findViewById(R.id.switchHidePausedApps);
        spinnerSearchBarPosition = findViewById(R.id.spinnerSearchBarPosition);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_custom_spinner, new String[]{"Bottom", "Top"});

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerSearchBarPosition.setAdapter(adapter);

        // Retrieve Saved Preferences
        boolean alwaysShowKeyboard = (boolean) SharedPreferencesHelper.getData(this, "AppDrawerAlwaysShowKeyboard", false);
        switchAlwaysShowKeyboard.setChecked(alwaysShowKeyboard);



        // Update The Preferences on Change
        switchAlwaysShowKeyboard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the preference when the switch state changes
                SharedPreferencesHelper.saveData(AppDrawerSettingsActivity.this, "AppDrawerAlwaysShowKeyboard", isChecked);
            }
        });

    }
}