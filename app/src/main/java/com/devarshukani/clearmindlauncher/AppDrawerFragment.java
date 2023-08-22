package com.devarshukani.clearmindlauncher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

//         Request focus and show the keyboard for the search bar
//        searchEditText.requestFocus();
//        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean alwaysShowKeyboard = (boolean) SharedPreferencesHelper.getData(getContext(), "AppDrawerAlwaysShowKeyboard", false);

        if (alwaysShowKeyboard) {
            // Request focus and show the keyboard for the search bar
            searchEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        getContext().registerReceiver(appInstallReceiver, filter);

//        loadApps();
//        setupRecyclerView(getView());
//        setupSearchBar();
    }

    private BroadcastReceiver appInstallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) ||
                        intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {

                    // Reload the app list
                    loadApps();
                    setupRecyclerView(getView());

                }
            }
        }
    };


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
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            searchEditText.clearFocus();
            searchEditText.setText("");
            startActivity(launchIntent);
        }


    }

    private void showCustomDialog(AppDrawerFragment.AppListItem app) {
        // Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_app_info, null);

        ImageView imageViewAppIcon = dialogView.findViewById(R.id.imageViewAppIcon);
        TextView textViewAppName = dialogView.findViewById(R.id.textViewAppName);
        TextView buttonAppInfo = dialogView.findViewById(R.id.buttonAppInfo);

        // Set app information in the dialog views
        imageViewAppIcon.setImageDrawable((Drawable) app.icon);
        textViewAppName.setText(app.name);

        // Build and show the custom dialog with the custom style
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        dialogBuilder.setView(dialogView);

        AlertDialog dialog = dialogBuilder.create();

        // Set the click listener for the "App Info" button
        buttonAppInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + app.label.toString()));
                startActivity(intent);

                dialog.dismiss(); // Dismiss the dialog after opening app settings
            }
        });

        dialog.show();
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

            // Long-press action to show the custom dialog
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showCustomDialog(app);
                    return true; // Consume the event
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
