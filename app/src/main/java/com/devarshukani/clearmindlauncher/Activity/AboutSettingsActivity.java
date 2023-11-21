package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.R;

public class AboutSettingsActivity extends AppCompatActivity {

    LinearLayout ButtonReportBug, ButtonFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_settings);

        ButtonReportBug = findViewById(R.id.ButtonReportBug);
        ButtonFeedback = findViewById(R.id.ButtonFeedback);

        AnimateLinearLayoutButton anim = new AnimateLinearLayoutButton();


        ButtonFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anim.animateButtonClick(ButtonFeedback);

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
                anim.animateButtonClick(ButtonReportBug);

                String email = "connect.dreamwave@gmail.com";
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