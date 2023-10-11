package com.devarshukani.clearmindlauncher.Adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devarshukani.clearmindlauncher.Fragment.AppDrawerFragment;
import com.devarshukani.clearmindlauncher.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private Context context;
    private List<AppDrawerFragment.AppListItem> appList;
    private List<Boolean> checkedStates;
    private PackageManager packageManager;

    private static final int MAX_SELECTIONS = 5;
    private int selectedCount = 0;
    private List<AppDrawerFragment.AppListItem> selectedApps;

    public AppListAdapter(Context context, List<AppDrawerFragment.AppListItem> appList, List<AppDrawerFragment.AppListItem> selectedApps) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.appList = appList;
        this.checkedStates = new ArrayList<>(Collections.nCopies(appList.size(), false));
        this.selectedApps = selectedApps;
        selectedCount = selectedApps.size();
        setInitialCheckboxStates();
    }

    private void setInitialCheckboxStates() {
        for (int i = 0; i < appList.size(); i++) {
            AppDrawerFragment.AppListItem appInfo = appList.get(i);
            checkedStates.set(i, isSelectedApp(appInfo));
        }
    }

    private boolean isSelectedApp(AppDrawerFragment.AppListItem appInfo) {
        if (selectedApps != null) {
            for (AppDrawerFragment.AppListItem selectedApp : selectedApps) {
                if (selectedApp.label.equals(appInfo.label) && selectedApp.name.equals(appInfo.name)) {
                    return true;
                }
            }
        }
        return false;
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
        holder.appCheckBox.setChecked(checkedStates.get(position));

        holder.appCheckBox.setOnClickListener(v -> {
            if (checkedStates.get(position)) {
                checkedStates.set(position, false);
                selectedCount--;
            } else if (selectedCount < MAX_SELECTIONS) {
                checkedStates.set(position, true);
                selectedCount++;
            }

            notifyDataSetChanged();
            // Do any additional logic here (e.g., saving the updated list of selected apps)
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public List<AppDrawerFragment.AppListItem> getSelectedApps() {
        List<AppDrawerFragment.AppListItem> selectedApps = new ArrayList<>();
        for (int i = 0; i < appList.size(); i++) {
            if (checkedStates.get(i)) {
                selectedApps.add(appList.get(i));
            }
        }
        return selectedApps;
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
