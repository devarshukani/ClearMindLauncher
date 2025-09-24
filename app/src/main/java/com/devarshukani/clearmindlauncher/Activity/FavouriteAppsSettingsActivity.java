package com.devarshukani.clearmindlauncher.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.devarshukani.clearmindlauncher.Adapter.AppListAdapter;
import com.devarshukani.clearmindlauncher.Utils.OnCheckedChangeListener;
import com.devarshukani.clearmindlauncher.Fragment.AppDrawerFragment;
import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavouriteAppsSettingsActivity extends AppCompatActivity implements OnCheckedChangeListener {

    private RecyclerView recyclerView;
    private AppListAdapter adapter;
    private TextView textViewSelectedCount;
    private EditText searchEditText;
    private AnimateLinearLayoutButton animHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_favourite_apps_settings);

        // Handle window insets for safe areas
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply padding to avoid status bar and navigation bar overlap
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });

        animHelper = new AnimateLinearLayoutButton();

        textViewSelectedCount = findViewById(R.id.textViewSelectedCount);
        recyclerView = findViewById(R.id.app_list_recycler_view);
        searchEditText = findViewById(R.id.ETSearchField);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve selected apps from SharedPreferences
        List<AppDrawerFragment.AppListItem> selectedApps = getSelectedAppsFromSharedPreferences();
        List<AppDrawerFragment.AppListItem> appList = getAppsInAppDrawer();
        adapter = new AppListAdapter(this, appList, selectedApps);
        adapter.setOnCheckedChangeListener(this);
        textViewSelectedCount.setText(String.valueOf(adapter.getSelectedCount()));
        recyclerView.setAdapter(adapter);

        // Setup drag and drop for reordering selected apps
        setupDragAndDrop();

        setupSearchBar();
    }

    private void setupSearchBar() {
        // Add haptic feedback when search bar gets focus
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animHelper.animateButtonClickWithHaptics(searchEditText);
            }
        });

        // Add search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private List<AppDrawerFragment.AppListItem> getAppsInAppDrawer() {
        PackageManager packageManager = getPackageManager();
        List<AppDrawerFragment.AppListItem> apps = new ArrayList<>();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);

        String myPackage = getPackageName();
        for (ResolveInfo ri : availableActivities) {
            // Skip the launcher app itself
            if (ri.activityInfo.packageName != null && ri.activityInfo.packageName.equals(myPackage)) {
                continue;
            }

            AppDrawerFragment.AppListItem app = new AppDrawerFragment.AppListItem();
            app.label = ri.activityInfo.packageName;
            app.name = ri.loadLabel(packageManager);
            app.icon = ri.loadIcon(packageManager);

            apps.add(app);
        }

        Collections.sort(apps, (app1, app2) ->
            app1.name.toString().compareToIgnoreCase(app2.name.toString())
        );

        return apps;
    }

    // Save selected apps to SharedPreferences
    private void saveSelectedAppsToSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("SelectedApps", Context.MODE_PRIVATE);

        // Create a StringBuilder to build the formatted string
        StringBuilder selectedAppsString = new StringBuilder();

        for (AppDrawerFragment.AppListItem app : adapter.getSelectedApps()) {
            // Format: appName|appLabel
            selectedAppsString.append(app.name).append("|").append(app.label).append(",");
        }

        // Remove the trailing comma if it exists
        if (selectedAppsString.length() > 0) {
            selectedAppsString.setLength(selectedAppsString.length() - 1);
        }

        // Save the formatted string to SharedPreferences
        sharedPreferences.edit().putString("selected_apps", selectedAppsString.toString()).apply();
    }

    // Retrieve selected apps from SharedPreferences
    private List<AppDrawerFragment.AppListItem> getSelectedAppsFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("SelectedApps", Context.MODE_PRIVATE);
        String selectedAppsString = sharedPreferences.getString("selected_apps", null);
        Log.d("Debug", "SelectedAppsString: " + selectedAppsString);
        List<AppDrawerFragment.AppListItem> selectedApps = new ArrayList<>();

        String myPackage = getPackageName();
        if (selectedAppsString != null && !selectedAppsString.isEmpty()) {
            String[] appStrings = selectedAppsString.split(",");
            for (String appString : appStrings) {
                String[] appData = appString.split("\\|");
                if (appData.length == 2) {
                    String savedName = appData[0];
                    String savedLabel = appData[1];

                    // Skip if the saved entry is this launcher
                    if (savedLabel != null && savedLabel.equals(myPackage)) {
                        continue;
                    }

                    AppDrawerFragment.AppListItem app = new AppDrawerFragment.AppListItem();
                    app.name = savedName;
                    app.label = savedLabel;
                    selectedApps.add(app);
                }
            }
        }

        return selectedApps;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSelectedAppsToSharedPreferences();
    }

    @Override
    public void onItemCheckedChanged(int count) {
        textViewSelectedCount.setText(String.valueOf(count));
    }

    // Setup drag and drop for reordering selected apps
    private void setupDragAndDrop() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Only allow reordering within selected apps section
                if (adapter.canMoveItem(fromPosition) && adapter.canMoveItem(toPosition)) {
                    adapter.moveItem(fromPosition, toPosition);
                    return true;
                }
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe action needed
            }

            @Override
            public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
                // Only allow dropping on selected apps (before divider)
                return adapter.canMoveItem(target.getAdapterPosition());
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
                    // Provide haptic feedback when drag starts
                    animHelper.animateButtonClickWithHaptics(viewHolder.itemView);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    // Define social media and distracting apps
    private Set<String> getDistractingApps() {
        return new HashSet<>(Arrays.asList(
            // Social & Content Feeds
            "com.instagram.android",
            "com.ss.android.ugc.trill",
            "com.zhiliaoapp.musically",
            "com.facebook.katana",
            "com.snapchat.android",
            "com.twitter.android",
            "com.reddit.frontpage",
            "com.pinterest",
            // Video & Streaming
            "com.google.android.youtube",
            "tv.twitch.android.app",
            "com.netflix.mediaclient",
            "com.amazon.avod.thirdpartyclient",
            "in.startv.hotstar.dplus",
            "com.disney.disneyplus",
            "com.hulu.plus"
        ));
    }

    private boolean isDistractingApp(String packageName) {
        return getDistractingApps().contains(packageName);
    }

    // Check if user is trying to add a distracting app and show reality check dialog
    public boolean checkAndShowRealityDialog(AppDrawerFragment.AppListItem app) {
        if (isDistractingApp(app.label.toString())) {
            showRealityCheckDialog(app);
            return true; // Block the selection
        }
        return false; // Allow the selection
    }

    private void showRealityCheckDialog(AppDrawerFragment.AppListItem app) {
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.dialog_reality_check, null);

        TextView messageTextView = bottomSheetView.findViewById(R.id.textViewRealityCheckMessage);
        Button buttonChangedMind = bottomSheetView.findViewById(R.id.buttonChangedMind);
        TextView buttonAddAnyway = bottomSheetView.findViewById(R.id.buttonAddAnyway);
        TextView buttonUninstallLauncher = bottomSheetView.findViewById(R.id.buttonUninstallLauncher);

        // Set the message with app name
        String message = String.format("Adding %s to your Favorites is basically giving distractions a VIP pass. If you value your time, don't add it. If you insist, we'll add it — but don't blame the launcher when your day disappears.", app.name);
        messageTextView.setText(message);

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);

        buttonChangedMind.setOnClickListener(v -> {
            animHelper.animateButtonClickWithHaptics(buttonChangedMind);
            bottomSheetDialog.dismiss();
        });

        buttonAddAnyway.setOnClickListener(v -> {
            animHelper.animateButtonClickWithHaptics(buttonAddAnyway);
            // Force add the app despite warning
            adapter.forceAddApp(app);
            onItemCheckedChanged(adapter.getSelectedCount());
            bottomSheetDialog.dismiss();
        });

        buttonUninstallLauncher.setOnClickListener(v -> {
            animHelper.animateButtonClickWithHaptics(buttonUninstallLauncher);
            // Uninstall the launcher
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
}
