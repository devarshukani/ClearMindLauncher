package com.devarshukani.clearmindlauncher.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper;
import com.devarshukani.clearmindlauncher.R;

public class Onboarding1Activity extends AppCompatActivity {

    private static final String FIRST_TIME_KEY = "first_time";

    LinearLayout buttonNext1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding1);

        buttonNext1 = findViewById(R.id.buttonNext1);

        // Check if it's the first time launch
        boolean isFirstTime = (boolean) SharedPreferencesHelper.getData(this, FIRST_TIME_KEY, true);

        if (!isFirstTime) {

            Intent intent = new Intent(this, LauncherActivity.class); // Replace LauncherClass with your actual launcher class
            startActivity(intent);
            finish();

        }

        buttonNext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Onboarding2Activity.class);
                startActivity(intent);
            }
        });

    }
}
