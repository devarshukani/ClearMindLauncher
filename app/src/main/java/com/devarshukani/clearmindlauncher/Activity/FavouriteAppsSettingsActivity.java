package com.devarshukani.clearmindlauncher.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.devarshukani.clearmindlauncher.Adapter.AppListAdapter;
import com.devarshukani.clearmindlauncher.Fragment.AppDrawerFragment;
import com.devarshukani.clearmindlauncher.R;
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

        List<AppDrawerFragment.AppListItem> appList = getAppsInAppDrawer();
        adapter = new AppListAdapter(this, appList);
        recyclerView.setAdapter(adapter);
    }

    private List<AppDrawerFragment.AppListItem> getAppsInAppDrawer() {
        PackageManager packageManager = getPackageManager();
//        List<ApplicationInfo> appList = new ArrayList<>();
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
}
