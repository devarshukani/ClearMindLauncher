package com.devarshukani.clearmindlauncher.Activity;

import static com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper.saveData;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper;
import com.devarshukani.clearmindlauncher.R;

public class ClockFaceSettingsActivity extends AppCompatActivity {

    Button clock1button, clock2button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_face_settings);

        clock1button = findViewById(R.id.clock1button);
        clock2button = findViewById(R.id.clock2button);

        clock1button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ClockFaceSettingsActivity.this, "Click 1", Toast.LENGTH_SHORT).show();
                SharedPreferencesHelper.saveData(ClockFaceSettingsActivity.this,"SelectedClockFaceNumber", 1);
            }
        });

        clock2button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ClockFaceSettingsActivity.this, "Click 2", Toast.LENGTH_SHORT).show();
                SharedPreferencesHelper.saveData(ClockFaceSettingsActivity.this,"SelectedClockFaceNumber", 2);
            }
        });
    }
}