package com.devarshukani.clearmindlauncher.Activity;

import static com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper.saveData;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper;
import com.devarshukani.clearmindlauncher.R;

public class ClockFaceSettingsActivity extends AppCompatActivity {

    LinearLayout clock1button, clock2button, clock3button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock_face_settings);

        clock1button = findViewById(R.id.clock1button);
        clock2button = findViewById(R.id.clock2button);
        clock3button = findViewById(R.id.clock3button);

        AnimateLinearLayoutButton anim = new AnimateLinearLayoutButton();

        clock1button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(clock1button);

                Toast.makeText(ClockFaceSettingsActivity.this, "Classic Clock Applied", Toast.LENGTH_SHORT).show();
                SharedPreferencesHelper.saveData(ClockFaceSettingsActivity.this,"SelectedClockFaceNumber", 1);
            }
        });

        clock2button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(clock2button);

                Toast.makeText(ClockFaceSettingsActivity.this, "Vertical Classic Clock Applied", Toast.LENGTH_SHORT).show();
                SharedPreferencesHelper.saveData(ClockFaceSettingsActivity.this,"SelectedClockFaceNumber", 2);
            }
        });

        clock3button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(clock3button);

                Toast.makeText(ClockFaceSettingsActivity.this, "Horizontal Clock Applied", Toast.LENGTH_SHORT).show();
                SharedPreferencesHelper.saveData(ClockFaceSettingsActivity.this,"SelectedClockFaceNumber", 3);
            }
        });
    }
}