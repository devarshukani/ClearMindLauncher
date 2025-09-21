package com.devarshukani.clearmindlauncher.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devarshukani.clearmindlauncher.Fragment.AppDrawerFragment;
import com.devarshukani.clearmindlauncher.R;
import com.devarshukani.clearmindlauncher.Utils.OnCheckedChangeListener;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_APP = 0;
    private static final int TYPE_DIVIDER = 1;

    private final Context context;
    private final List<AppDrawerFragment.AppListItem> originalAppList;
    private List<Object> displayList; // Mixed list of apps and dividers
    private OnCheckedChangeListener listener;

    private static final int MAX_SELECTIONS = 6;
    private int selectedCount;
    private final List<AppDrawerFragment.AppListItem> selectedApps;

    public AppListAdapter(Context context, List<AppDrawerFragment.AppListItem> appList, List<AppDrawerFragment.AppListItem> selectedApps) {
        this.context = context;
        this.originalAppList = new ArrayList<>(appList);
        this.selectedApps = selectedApps;
        selectedCount = selectedApps.size();

        // Initialize display list with sorted apps
        updateDisplayList(appList);
    }

    private void updateDisplayList(List<AppDrawerFragment.AppListItem> appList) {
        displayList = new ArrayList<>();

        List<AppDrawerFragment.AppListItem> selectedAppsList = new ArrayList<>();
        List<AppDrawerFragment.AppListItem> unselectedAppsList = new ArrayList<>();

        // Separate selected and unselected apps
        for (AppDrawerFragment.AppListItem app : appList) {
            if (isSelectedApp(app)) {
                selectedAppsList.add(app);
            } else {
                unselectedAppsList.add(app);
            }
        }

        // Add selected apps first
        displayList.addAll(selectedAppsList);

        // Add divider if there are both selected and unselected apps
        if (!selectedAppsList.isEmpty() && !unselectedAppsList.isEmpty()) {
            displayList.add("DIVIDER");
        }

        // Add unselected apps
        displayList.addAll(unselectedAppsList);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
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

    public void filter(String searchText) {
        List<AppDrawerFragment.AppListItem> filteredApps = new ArrayList<>();

        if (searchText == null || searchText.trim().isEmpty()) {
            filteredApps.addAll(originalAppList);
        } else {
            String searchLower = searchText.toLowerCase().trim();
            for (AppDrawerFragment.AppListItem app : originalAppList) {
                if (app.name.toString().toLowerCase().contains(searchLower)) {
                    filteredApps.add(app);
                }
            }
        }

        updateDisplayList(filteredApps);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (displayList.get(position) instanceof String) {
            return TYPE_DIVIDER;
        }
        return TYPE_APP;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DIVIDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_divider, parent, false);
            return new DividerViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_app_list_checkbox, parent, false);
            return new AppViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AppViewHolder) {
            AppDrawerFragment.AppListItem appInfo = (AppDrawerFragment.AppListItem) displayList.get(position);
            AppViewHolder appHolder = (AppViewHolder) holder;

            appHolder.appName.setText(appInfo.name);

            // Check if this app is selected by comparing with the selectedApps list
            boolean isChecked = isSelectedApp(appInfo);
            appHolder.appCheckBox.setChecked(isChecked);

            appHolder.appCheckBox.setOnClickListener(v -> {
                if (isSelectedApp(appInfo)) {
                    // Remove from selected apps
                    removeFromSelectedApps(appInfo);
                    selectedCount--;
                } else if (selectedCount < MAX_SELECTIONS) {
                    // Add to selected apps
                    addToSelectedApps(appInfo);
                    selectedCount++;
                }

                if (listener != null) {
                    listener.onItemCheckedChanged(selectedCount);
                }

                // Rebuild the display list to move selected items to top
                List<AppDrawerFragment.AppListItem> currentApps = getCurrentAppList();
                updateDisplayList(currentApps);
                notifyDataSetChanged();
            });
        }
    }

    private void addToSelectedApps(AppDrawerFragment.AppListItem app) {
        // Check if not already in selected apps
        if (!isSelectedApp(app)) {
            selectedApps.add(app);
        }
    }

    private void removeFromSelectedApps(AppDrawerFragment.AppListItem app) {
        selectedApps.removeIf(selectedApp ->
            selectedApp.label.equals(app.label) && selectedApp.name.equals(app.name));
    }

    private List<AppDrawerFragment.AppListItem> getCurrentAppList() {
        List<AppDrawerFragment.AppListItem> currentApps = new ArrayList<>();
        for (Object item : displayList) {
            if (item instanceof AppDrawerFragment.AppListItem) {
                currentApps.add((AppDrawerFragment.AppListItem) item);
            }
        }
        return currentApps;
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public List<AppDrawerFragment.AppListItem> getSelectedApps() {
        return new ArrayList<>(selectedApps);
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        CheckBox appCheckBox;
        TextView appName;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appCheckBox = itemView.findViewById(R.id.app_checkbox);
            appName = itemView.findViewById(R.id.app_name);
        }
    }

    public static class DividerViewHolder extends RecyclerView.ViewHolder {
        public DividerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
