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
import com.devarshukani.clearmindlauncher.Activity.FavouriteAppsSettingsActivity;

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

        // First, add selected apps in their saved order from selectedApps list
        for (AppDrawerFragment.AppListItem selectedApp : selectedApps) {
            for (AppDrawerFragment.AppListItem app : appList) {
                if (app.label.equals(selectedApp.label) && app.name.equals(selectedApp.name)) {
                    selectedAppsList.add(app);
                    break;
                }
            }
        }

        // Then add unselected apps
        for (AppDrawerFragment.AppListItem app : appList) {
            if (!isSelectedApp(app)) {
                unselectedAppsList.add(app);
            }
        }

        // Add selected apps first (maintaining their order)
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

                    if (listener != null) {
                        listener.onItemCheckedChanged(selectedCount);
                    }

                    // Rebuild the display list to move selected items to top
                    List<AppDrawerFragment.AppListItem> currentApps = getCurrentAppList();
                    updateDisplayList(currentApps);
                    notifyDataSetChanged();
                } else if (selectedCount < MAX_SELECTIONS) {
                    // Check if this is a distracting app first
                    if (context instanceof FavouriteAppsSettingsActivity) {
                        FavouriteAppsSettingsActivity activity = (FavouriteAppsSettingsActivity) context;
                        if (activity.checkAndShowRealityDialog(appInfo)) {
                            // Reality check dialog shown, reset checkbox to unchecked state
                            appHolder.appCheckBox.setChecked(false);
                            return;
                        }
                    }

                    // Add to selected apps (normal apps or user chose "Add anyway")
                    addToSelectedApps(appInfo);
                    selectedCount++;

                    if (listener != null) {
                        listener.onItemCheckedChanged(selectedCount);
                    }

                    // Rebuild the display list to move selected items to top
                    List<AppDrawerFragment.AppListItem> currentApps = getCurrentAppList();
                    updateDisplayList(currentApps);
                    notifyDataSetChanged();
                }
            });
        }
    }

    // Method to force add an app (used when user chooses "Add anyway" from reality check dialog)
    public void forceAddApp(AppDrawerFragment.AppListItem app) {
        if (selectedCount < MAX_SELECTIONS && !isSelectedApp(app)) {
            addToSelectedApps(app);
            selectedCount++;

            // Rebuild the display list to move selected items to top
            List<AppDrawerFragment.AppListItem> currentApps = getCurrentAppList();
            updateDisplayList(currentApps);
            notifyDataSetChanged();
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

    public void moveItem(int fromPosition, int toPosition) {
        // Only allow reordering within selected apps section (before divider)
        if (fromPosition < getSelectedAppCount() && toPosition < getSelectedAppCount()) {
            // Move within the selectedApps list
            AppDrawerFragment.AppListItem movedApp = selectedApps.remove(fromPosition);
            selectedApps.add(toPosition, movedApp);

            // Update display list
            Object movedItem = displayList.remove(fromPosition);
            displayList.add(toPosition, movedItem);

            notifyItemMoved(fromPosition, toPosition);
            return;
        }
    }

    public boolean canMoveItem(int position) {
        // Only allow moving selected apps (items before the divider)
        return position < getSelectedAppCount();
    }

    private int getSelectedAppCount() {
        int count = 0;
        for (Object item : displayList) {
            if (item instanceof String && "DIVIDER".equals(item)) {
                break;
            }
            if (item instanceof AppDrawerFragment.AppListItem) {
                count++;
            }
        }
        return count;
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
