package com.devarshukani.clearmindlauncher.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.devarshukani.clearmindlauncher.Adapter.AppListAdapter;
import com.devarshukani.clearmindlauncher.Utils.OnCheckedChangeListener;
import com.devarshukani.clearmindlauncher.Fragment.AppDrawerFragment;
import com.devarshukani.clearmindlauncher.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FavouriteAppsSettingsActivity extends AppCompatActivity implements OnCheckedChangeListener {

    private RecyclerView recyclerView;
    private AppListAdapter adapter;
    private TextView textViewSelectedCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_apps_settings);

        textViewSelectedCount = findViewById(R.id.textViewSelectedCount);
        recyclerView = findViewById(R.id.app_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve selected apps from SharedPreferences
        List<AppDrawerFragment.AppListItem> selectedApps = getSelectedAppsFromSharedPreferences();
        List<AppDrawerFragment.AppListItem> appList = getAppsInAppDrawer();
        adapter = new AppListAdapter(this, appList, selectedApps);
        adapter.setOnCheckedChangeListener(this);
        textViewSelectedCount.setText(String.valueOf(adapter.getSelectedCount()));
        recyclerView.setAdapter(adapter);
    }

    private List<AppDrawerFragment.AppListItem> getAppsInAppDrawer() {
        PackageManager packageManager = getPackageManager();
        List<AppDrawerFragment.AppListItem> apps = new ArrayList<>();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);

        for (ResolveInfo ri : availableActivities) {
            AppDrawerFragment.AppListItem app = new AppDrawerFragment.AppListItem();
            app.label = ri.activityInfo.packageName;
            app.name = ri.loadLabel(packageManager);
            app.icon = ri.loadIcon(packageManager);

            apps.add(app);
        }

        Collections.sort(apps, new Comparator<AppDrawerFragment.AppListItem>() {
            @Override
            public int compare(AppDrawerFragment.AppListItem app1, AppDrawerFragment.AppListItem app2) {
                return app1.name.toString().compareToIgnoreCase(app2.name.toString());
            }
        });

        return apps;
    }

    // Save selected apps to SharedPreferences
    private void saveSelectedAppsToSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("SelectedApps", Context.MODE_PRIVATE);

        // Create a StringBuilder to build the formatted string
        StringBuilder selectedAppsString = new StringBuilder();

        for (AppDrawerFragment.AppListItem app : adapter.getSelectedApps()) {
            // Format: appName|appLabel
            selectedAppsString.append(app.name).append("|").append(app.label).append(",");
        }

        // Remove the trailing comma if it exists
        if (selectedAppsString.length() > 0) {
            selectedAppsString.setLength(selectedAppsString.length() - 1);
        }

        // Save the formatted string to SharedPreferences
        sharedPreferences.edit().putString("selected_apps", selectedAppsString.toString()).apply();
    }

    // Retrieve selected apps from SharedPreferences
    private List<AppDrawerFragment.AppListItem> getSelectedAppsFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("SelectedApps", Context.MODE_PRIVATE);
        String selectedAppsString = sharedPreferences.getString("selected_apps", null);
        Log.d("Debug", "SelectedAppsString: " + selectedAppsString);
        List<AppDrawerFragment.AppListItem> selectedApps = new ArrayList<>();

        if (selectedAppsString != null && !selectedAppsString.isEmpty()) {
            String[] appStrings = selectedAppsString.split(",");
            for (String appString : appStrings) {
                String[] appData = appString.split("\\|");
                if (appData.length == 2) {
                    AppDrawerFragment.AppListItem app = new AppDrawerFragment.AppListItem();
                    app.name = appData[0];
                    app.label = appData[1];
                    selectedApps.add(app);
                }
            }
        }

        return selectedApps;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSelectedAppsToSharedPreferences();
    }

    @Override
    public void onItemCheckedChanged(int count) {
        textViewSelectedCount.setText(String.valueOf(count));
    }
}
