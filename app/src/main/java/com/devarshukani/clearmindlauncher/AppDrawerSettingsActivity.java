package com.devarshukani.clearmindlauncher;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class AppDrawerSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_drawer_settings);

        Spinner spinnerSearchBarPosition = findViewById(R.id.spinnerSearchBarPosition);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_custom_spinner, new String[]{"bottom", "top"});

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerSearchBarPosition.setAdapter(adapter);

    }
}