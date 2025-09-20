package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;

import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.R;

public class PermissionsSettingsActivity extends AppCompatActivity {

    LinearLayout ButtonPrivacyPolicy;
    LinearLayout buttonDisplayOverOtherApps;
    LinearLayout buttonUsageAccess;

    private AnimateLinearLayoutButton animHelper; // Add haptics helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_settings);

        animHelper = new AnimateLinearLayoutButton(); // Initialize haptics helper

        buttonDisplayOverOtherApps = findViewById(R.id.buttonDisplayOverOtherApps);
        buttonUsageAccess = findViewById(R.id.buttonUsageAccess);

        ButtonPrivacyPolicy = findViewById(R.id.ButtonPrivacyPolicy);

        buttonDisplayOverOtherApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(buttonDisplayOverOtherApps); // Add haptic feedback
                // Open the screen to grant 'Display Over Other Apps' permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });



        buttonUsageAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(buttonUsageAccess); // Add haptic feedback
                // Open the screen to grant 'Usage Access' permission
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        });



        ButtonPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonPrivacyPolicy); // Add haptic feedback
                String privacyPolicyURL = "https://doc-hosting.flycricket.io/clearmind-launcher-privacy-policy/3100f44e-70a2-49ce-9805-db0d87143fc4/privacy";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyURL));
                startActivity(browserIntent);
            }
        });



    }
}