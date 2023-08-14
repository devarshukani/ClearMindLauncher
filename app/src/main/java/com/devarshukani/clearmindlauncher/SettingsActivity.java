package com.devarshukani.clearmindlauncher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class SettingsActivity extends AppCompatActivity {

    LinearLayout ButtonCustomization;
    LinearLayout ButtonHomePage;
    LinearLayout ButtonAppDrawer;
    LinearLayout ButtonPermissions;
    LinearLayout ButtonAbout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButtonCustomization = findViewById(R.id.ButtonCustomization);
        ButtonHomePage = findViewById(R.id.ButtonHomePage);
        ButtonAppDrawer = findViewById(R.id.ButtonAppDrawer);
        ButtonPermissions = findViewById(R.id.ButtonPermissions);
        ButtonAbout = findViewById(R.id.ButtonPermissions);


        ButtonAppDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, AppDrawerSettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}