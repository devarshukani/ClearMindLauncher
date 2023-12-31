package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper;
import com.devarshukani.clearmindlauncher.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class AppPauseControlsSettingsActivity extends AppCompatActivity {

    MaterialSwitch switchTemporaryAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_pause_controls_settings);

        switchTemporaryAccess = findViewById(R.id.switchTemporaryAccess);


        boolean temporaryAccess = (boolean) SharedPreferencesHelper.getData(this, "AppPauseControlsTemporaryAccess", false);
        switchTemporaryAccess.setChecked(temporaryAccess);

        switchTemporaryAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPreferencesHelper.saveData(AppPauseControlsSettingsActivity.this, "AppPauseControlsTemporaryAccess", isChecked);
            }
        });

    }
}