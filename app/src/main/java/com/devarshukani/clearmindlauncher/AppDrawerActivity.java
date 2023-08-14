package com.devarshukani.clearmindlauncher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppDrawerActivity extends AppCompatActivity {

    private PackageManager manager;
    private List<AppListItem> apps;
    private RecyclerView recyclerView;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_drawer);

        searchEditText = findViewById(R.id.ETHomeSearchField);
        loadApps();
        setupRecyclerView();
        setupSearchBar();

        // Request focus and show the keyboard for the search bar
        searchEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void loadApps() {
        manager = getPackageManager();
        apps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, PackageManager.GET_ACTIVITIES);

        for (ResolveInfo ri : availableActivities) {
            AppListItem app = new AppListItem();
            app.label = ri.activityInfo.packageName;
            app.name = ri.loadLabel(manager);
            app.icon = ri.loadIcon(manager);

            apps.add(app);
        }

        Collections.sort(apps, new Comparator<AppListItem>() {
            @Override
            public int compare(AppListItem app1, AppListItem app2) {
                return app1.name.toString().compareToIgnoreCase(app2.name.toString());
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        AppAdapter adapter = new AppAdapter(apps);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupSearchBar() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterApps(String searchText) {
        List<AppListItem> filteredApps = new ArrayList<>();

        for (AppListItem app : apps) {
            if (app.name.toString().toLowerCase().contains(searchText.toLowerCase())) {
                filteredApps.add(app);
            }
        }

        AppAdapter adapter = new AppAdapter(filteredApps);
        recyclerView.setAdapter(adapter);

        if (filteredApps.size() == 1) {
            launchApp(filteredApps.get(0));
        }
    }

    private void launchApp(AppListItem app) {
        Intent launchIntent = manager.getLaunchIntentForPackage(app.label.toString());
        if (launchIntent != null) {
            startActivity(launchIntent);
        }
    }

    private class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

        private List<AppListItem> appsList;

        public AppAdapter(List<AppListItem> appsList) {
            this.appsList = appsList;
        }

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_list, parent, false);
            return new AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            AppListItem app = appsList.get(position);
            holder.appName.setText(app.name);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchApp(app);
                }
            });
        }

        @Override
        public int getItemCount() {
            return appsList.size();
        }

        class AppViewHolder extends RecyclerView.ViewHolder {

            TextView appName;

            public AppViewHolder(@NonNull View itemView) {
                super(itemView);
                appName = itemView.findViewById(R.id.name);
            }
        }
    }

    private static class AppListItem {
        CharSequence label;
        CharSequence name;
        Object icon;
    }
}
