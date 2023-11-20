package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class HomeScreenSettingsActivity extends AppCompatActivity {

    LinearLayout ButtonFavouriteApps;
    LinearLayout ButtonClockFaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_settings);

        ButtonFavouriteApps = findViewById(R.id.ButtonFavouriteApps);
        ButtonClockFaces = findViewById(R.id.ButtonClockFaces);

        AnimateLinearLayoutButton anim = new AnimateLinearLayoutButton();

        ButtonFavouriteApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(ButtonFavouriteApps);


                Intent intent = new Intent(HomeScreenSettingsActivity.this, FavouriteAppsSettingsActivity.class);
                startActivity(intent);
            }
        });


        ButtonClockFaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(ButtonClockFaces);

                Intent intent = new Intent(HomeScreenSettingsActivity.this, ClockFaceSettingsActivity.class);
                startActivity(intent);

            }
        });

    }
}