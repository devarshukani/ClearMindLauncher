package com.devarshukani.clearmindlauncher.Adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
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
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private Context context;
    private List<AppDrawerFragment.AppListItem> appList;
    private List<AppDrawerFragment.AppListItem> selectedApps;
    private PackageManager packageManager;

    public AppListAdapter(Context context, List<AppDrawerFragment.AppListItem> appList) {
        this.context = context;
        this.packageManager = context.getPackageManager();
        this.appList = appList; // Remove the filter here
        this.selectedApps = new ArrayList<>();
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
        holder.appCheckBox.setChecked(selectedApps.contains(appInfo));

        holder.appCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (selectedApps.size() < 5) {
                    selectedApps.add(appInfo);
                } else {
                    holder.appCheckBox.setChecked(false);
                }
            } else {
                selectedApps.remove(appInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public List<AppDrawerFragment.AppListItem> getSelectedApps() {
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
