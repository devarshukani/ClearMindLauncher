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

    LinearLayout ButtonDefaultLauncher;
    LinearLayout ButtonPrivacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_settings);

        ButtonDefaultLauncher = findViewById(R.id.ButtonDefaultLauncher);
        ButtonPrivacyPolicy = findViewById(R.id.ButtonPrivacyPolicy);

        AnimateLinearLayoutButton anim = new AnimateLinearLayoutButton();

        ButtonDefaultLauncher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(ButtonDefaultLauncher);
                Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                startActivity(intent);
            }
        });

        ButtonPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(ButtonPrivacyPolicy);
                String privacyPolicyURL = "https://doc-hosting.flycricket.io/clearmind-launcher-privacy-policy/3100f44e-70a2-49ce-9805-db0d87143fc4/privacy";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyURL));
                startActivity(browserIntent);
            }
        });



    }
}