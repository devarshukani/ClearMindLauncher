package com.devarshukani.clearmindlauncher.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.devarshukani.clearmindlauncher.R;

public class Onboarding2Activity extends AppCompatActivity {

    LinearLayout buttonNext2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding2);

        buttonNext2 = findViewById(R.id.buttonNext2);

        buttonNext2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Onboarding3Activity.class);
                startActivity(intent);
            }
        });
    }
}