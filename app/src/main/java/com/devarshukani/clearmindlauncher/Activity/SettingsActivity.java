package com.devarshukani.clearmindlauncher.Activity;

import static androidx.core.view.ViewCompat.animate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;

import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.R;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout ButtonSystemSettings;
    LinearLayout ButtonCustomization;
    LinearLayout ButtonHomePage;
    LinearLayout ButtonAppDrawer;
    LinearLayout ButtonPermissions;
    LinearLayout ButtonAbout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButtonSystemSettings = findViewById(R.id.ButtonSystemSettings);
        ButtonCustomization = findViewById(R.id.ButtonCustomization);
        ButtonHomePage = findViewById(R.id.ButtonHomePage);
        ButtonAppDrawer = findViewById(R.id.ButtonAppDrawer);
        ButtonPermissions = findViewById(R.id.ButtonPermissions);
        ButtonAbout = findViewById(R.id.ButtonAbout);

        AnimateLinearLayoutButton anim = new AnimateLinearLayoutButton();


        ButtonSystemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(ButtonSystemSettings);

                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);

            }
        });


        ButtonHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(ButtonHomePage);

                Intent intent = new Intent(SettingsActivity.this, HomeScreenSettingsActivity.class);
                startActivity(intent);

            }
        });

        ButtonAppDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(ButtonAppDrawer);

                Intent intent = new Intent(SettingsActivity.this, AppDrawerSettingsActivity.class);
                startActivity(intent);
            }
        });


        ButtonPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(ButtonPermissions);

                Intent intent = new Intent(SettingsActivity.this, PermissionsSettingsActivity.class);
                startActivity(intent);

            }
        });

        ButtonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(ButtonAbout);

                Intent intent = new Intent(SettingsActivity.this, AboutSettingsActivity.class);
                startActivity(intent);
            }
        });

    }

}