package com.devarshukani.clearmindlauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class AppDrawerFragment extends Fragment{

    private PackageManager manager;
    private List<AppDrawerFragment.AppListItem> apps;
    private RecyclerView recyclerView;
    private EditText searchEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_drawer, container, false);
        // Add your logic and UI elements for the second fragment

        searchEditText = view.findViewById(R.id.ETHomeSearchField);
        loadApps();
        setupRecyclerView(view);
        setupSearchBar();

        // Request focus and show the keyboard for the search bar
        searchEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);


        return view;
    }

    private void loadApps() {
        manager = getContext().getPackageManager();
        apps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, PackageManager.GET_ACTIVITIES);

        for (ResolveInfo ri : availableActivities) {
            AppDrawerFragment.AppListItem app = new AppDrawerFragment.AppListItem();
            app.label = ri.activityInfo.packageName;
            app.name = ri.loadLabel(manager);
            app.icon = ri.loadIcon(manager);

            apps.add(app);
        }

        Collections.sort(apps, new Comparator<AppDrawerFragment.AppListItem>() {
            @Override
            public int compare(AppDrawerFragment.AppListItem app1, AppDrawerFragment.AppListItem app2) {
                return app1.name.toString().compareToIgnoreCase(app2.name.toString());
            }
        });
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        AppDrawerFragment.AppAdapter adapter = new AppDrawerFragment.AppAdapter(apps);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
        List<AppDrawerFragment.AppListItem> filteredApps = new ArrayList<>();

        for (AppDrawerFragment.AppListItem app : apps) {
            if (app.name.toString().toLowerCase().contains(searchText.toLowerCase())) {
                filteredApps.add(app);
            }
        }

        AppDrawerFragment.AppAdapter adapter = new AppDrawerFragment.AppAdapter(filteredApps);
        recyclerView.setAdapter(adapter);

        if (filteredApps.size() == 1) {
            launchApp(filteredApps.get(0));
        }
    }

    private void launchApp(AppDrawerFragment.AppListItem app) {
        Intent launchIntent = manager.getLaunchIntentForPackage(app.label.toString());
        if (launchIntent != null) {
            startActivity(launchIntent);
        }
    }

    private class AppAdapter extends RecyclerView.Adapter<AppDrawerFragment.AppAdapter.AppViewHolder> {

        private List<AppDrawerFragment.AppListItem> appsList;

        public AppAdapter(List<AppDrawerFragment.AppListItem> appsList) {
            this.appsList = appsList;
        }

        @NonNull
        @Override
        public AppDrawerFragment.AppAdapter.AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_list, parent, false);
            return new AppDrawerFragment.AppAdapter.AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AppDrawerFragment.AppAdapter.AppViewHolder holder, int position) {
            AppDrawerFragment.AppListItem app = appsList.get(position);
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
