package com.devarshukani.clearmindlauncher.Activity;

import static androidx.core.view.ViewCompat.animate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
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
    LinearLayout ButtonDefaultLauncher;

    LinearLayout ButtonHomePage;
    LinearLayout ButtonAppDrawer;
    LinearLayout ButtonPausedApps;

    LinearLayout ButtonPermissions;

    LinearLayout ButtonAbout;
    LinearLayout ButtonReportBug;
    LinearLayout ButtonFeedback;

    private AnimateLinearLayoutButton animHelper; // Add haptics helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        animHelper = new AnimateLinearLayoutButton(); // Initialize haptics helper

        ButtonSystemSettings = findViewById(R.id.ButtonSystemSettings);
        ButtonDefaultLauncher = findViewById(R.id.ButtonDefaultLauncher);

        ButtonHomePage = findViewById(R.id.ButtonHomePage);
        ButtonAppDrawer = findViewById(R.id.ButtonAppDrawer);
        ButtonPausedApps = findViewById(R.id.ButtonPausedApps);

        ButtonPermissions = findViewById(R.id.ButtonPermissions);

        ButtonAbout = findViewById(R.id.ButtonAbout);
        ButtonReportBug = findViewById(R.id.ButtonReportBug);
        ButtonFeedback = findViewById(R.id.ButtonFeedback);

        ButtonSystemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonSystemSettings); // Add haptic feedback

                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
            }
        });

        ButtonDefaultLauncher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonDefaultLauncher); // Add haptic feedback
                Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                startActivity(intent);
            }
        });

        ButtonHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonHomePage); // Add haptic feedback

                Intent intent = new Intent(SettingsActivity.this, HomeScreenSettingsActivity.class);
                startActivity(intent);
            }
        });

        ButtonAppDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonAppDrawer); // Add haptic feedback

                Intent intent = new Intent(SettingsActivity.this, AppDrawerSettingsActivity.class);
                startActivity(intent);
            }
        });

        ButtonPausedApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonPausedApps); // Add haptic feedback

                Intent intent = new Intent(SettingsActivity.this, AppPauseControlsSettingsActivity.class);
                startActivity(intent);
            }
        });

        ButtonPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonPermissions); // Add haptic feedback

                Intent intent = new Intent(SettingsActivity.this, PermissionsSettingsActivity.class);
                startActivity(intent);
            }
        });

        // ABOUT & FEEDBACK ------------------------------------------------------------------------

        ButtonAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonAbout); // Add haptic feedback

                Intent intent = new Intent(SettingsActivity.this, AboutSettingsActivity.class);
                startActivity(intent);
            }
        });

        ButtonFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonFeedback); // Add haptic feedback

                String appPackageName = getPackageName();

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

        ButtonReportBug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonReportBug); // Add haptic feedback

                String email = "devarsh@indi.technology";
                String subject = "Bug Report - ClearMind Launcher";
                String body = "Dear ClearMind Support Team,\n\nI encountered the following issue:\n[Describe the bug in detail here, attach screenshots if possible]\n\nSteps to reproduce the bug:\n1. \n2. \n3. \n\nDevice Information:\n- Device: [Your Device Model]\n- OS Version: [Your Android Version]\n\nYour prompt attention to this matter would be greatly appreciated.\n\nSincerely,\n[Your Name]";

                String uriText = "mailto:" + Uri.encode(email) +
                        "?subject=" + Uri.encode(subject) +
                        "&body=" + Uri.encode(body);

                Uri uri = Uri.parse(uriText);

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(uri);

                startActivity(intent);
            }
        });
    }
}