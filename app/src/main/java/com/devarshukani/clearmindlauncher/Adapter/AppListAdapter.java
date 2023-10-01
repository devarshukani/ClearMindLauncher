package com.devarshukani.clearmindlauncher.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devarshukani.clearmindlauncher.Fragment.AppDrawerFragment;
import com.devarshukani.clearmindlauncher.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private Context context;
    private List<AppDrawerFragment.AppListItem> appList;
    private List<AppDrawerFragment.AppListItem> selectedApps;
    private PackageManager packageManager;

    public AppListAdapter(Context context, List<AppDrawerFragment.AppListItem> appList, List<AppDrawerFragment.AppListItem> selectedApps) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.appList = appList;
        this.selectedApps = selectedApps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app_list_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppDrawerFragment.AppListItem appInfo = appList.get(position);
        holder.appName.setText(appInfo.name);



        // Check if the appInfo is in the selectedApps list
        boolean isSelected = false;

        for (AppDrawerFragment.AppListItem selectedApp : selectedApps) {
            if (selectedApp.label.equals(appInfo.label)) {
                isSelected = true;
                Log.d("DATA CHECK", appInfo.name.toString());
                break; // Exit the loop once a match is found
            }
        }

        // Set the checkbox state based on whether the app is selected or not
        holder.appCheckBox.setChecked(isSelected);

        holder.appCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (selectedApps.size() < 5) {
                    selectedApps.add(appInfo);
                } else {
                    holder.appCheckBox.setChecked(false);
                }
            } else {
                // Remove the selected app with a matching label
                for (AppDrawerFragment.AppListItem selectedApp : selectedApps) {
                    if (selectedApp.label.equals(appInfo.label)) {
                        selectedApps.remove(selectedApp);
                        break; // Exit the loop once removed
                    }
                }
            }

            updateSelectedAppsInSharedPreferences(); // Update SharedPreferences
        });
    }



    @Override
    public int getItemCount() {
        return appList.size();
    }

    public List<AppDrawerFragment.AppListItem> getSelectedApps() {
        return selectedApps;
    }

    private void updateSelectedAppsInSharedPreferences() {
        SharedPreferences preferences = context.getSharedPreferences("ClearMindSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        // Create a list of strings containing app names and package names
        List<String> selectedAppNamesAndPackages = new ArrayList<>();
        for (AppDrawerFragment.AppListItem appItem : selectedApps) {
            String appNameAndPackage = appItem.name.toString() + "|" + appItem.label;
            selectedAppNamesAndPackages.add(appNameAndPackage);
        }

        // Serialize the list to JSON and save it in SharedPreferences
        Gson gson = new Gson();
        String selectedAppsJson = gson.toJson(selectedAppNamesAndPackages);
        editor.putString("HomeScreenFavouriteSelectedAppsList", selectedAppsJson);
        editor.apply();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox appCheckBox;
        TextView appName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appCheckBox = itemView.findViewById(R.id.app_checkbox);
            appName = itemView.findViewById(R.id.app_name);
        }
    }
}
