package com.devarshukani.clearmindlauncher.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.devarshukani.clearmindlauncher.Adapter.AppListAdapter;
import com.devarshukani.clearmindlauncher.Fragment.AppDrawerFragment;
import com.devarshukani.clearmindlauncher.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FavouriteAppsSettingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_apps_settings);

        recyclerView = findViewById(R.id.app_list_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve selected apps from SharedPreferences and pass them to the adapter
        List<AppDrawerFragment.AppListItem> selectedApps = null;
        List<AppDrawerFragment.AppListItem> appList = getAppsInAppDrawer();
        adapter = new AppListAdapter(this, appList, selectedApps);
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


        return apps;
    }


}
