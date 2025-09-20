package com.devarshukani.clearmindlauncher.Fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.CalendarContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devarshukani.clearmindlauncher.Activity.SettingsActivity;
import com.devarshukani.clearmindlauncher.Database.PausedApps;
import com.devarshukani.clearmindlauncher.Database.RoomDB;
import com.devarshukani.clearmindlauncher.R;
import com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper;
import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppDrawerFragment extends Fragment{

    private PackageManager manager;
    private List<AppDrawerFragment.AppListItem> apps;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private ImageButton btnSettings;

    List<PausedApps> pausedAppsList;
    RoomDB database;
    private AnimateLinearLayoutButton animHelper; // Add helper for haptics

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_drawer, container, false);

        database = RoomDB.getInstance(getContext());
        pausedAppsList = database.mainDAO().getAll();
        animHelper = new AnimateLinearLayoutButton(); // Initialize haptics helper

        searchEditText = view.findViewById(R.id.ETHomeSearchField);
        btnSettings = view.findViewById(R.id.btnSettings);
        loadApps();
        setupRecyclerView(view);
        setupSearchBar();

//         Request focus and show the keyboard for the search bar
//        searchEditText.requestFocus();
//        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);

        // Add haptic feedback to search bar when focused
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    animHelper.animateButtonClickWithHaptics(searchEditText);
                }
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClickWithHaptics(btnSettings); // Add haptic feedback
                Intent intent = new Intent(getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        database = RoomDB.getInstance(getContext());
        pausedAppsList = database.mainDAO().getAll();

        boolean alwaysShowKeyboard = (boolean) SharedPreferencesHelper.getData(getContext(), "AppDrawerAlwaysShowKeyboard", false);

        if (alwaysShowKeyboard) {
            // Request focus and show the keyboard for the search bar
            searchEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        }

        boolean showAppIcons = (boolean) SharedPreferencesHelper.getData(getContext(), "AppDrawerShowAppIcons", false);
        AppAdapter adapter = (AppAdapter) recyclerView.getAdapter();

        if (adapter != null) {
            adapter.updateAppIconVisibility(showAppIcons);
        }


        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        getContext().registerReceiver(appInstallReceiver, filter);

        loadApps();
        setupRecyclerView(getView());
        setupSearchBar();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (recyclerView != null) {
            recyclerView.scrollToPosition(0);
        }
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

        // Get the current setting for showing app icons
        boolean showAppIcons = (boolean) SharedPreferencesHelper.getData(getContext(), "AppDrawerShowAppIcons", false);

        for (ResolveInfo ri : availableActivities) {
            AppDrawerFragment.AppListItem app = new AppDrawerFragment.AppListItem();
            app.label = ri.activityInfo.packageName;
            app.name = ri.loadLabel(manager);
            app.icon = ri.loadIcon(manager);
            app.showIcon = showAppIcons; // Initialize showIcon field

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
        AppAdapter adapter = new AppAdapter(apps, pausedAppsList); // Pass pausedAppsList here
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

        // Get the current setting for showing app icons
        boolean showAppIcons = (boolean) SharedPreferencesHelper.getData(getContext(), "AppDrawerShowAppIcons", false);

        AppDrawerFragment.AppAdapter adapter = new AppDrawerFragment.AppAdapter(filteredApps, pausedAppsList);
        // Update icon visibility for the new adapter
        adapter.updateAppIconVisibility(showAppIcons);
        recyclerView.setAdapter(adapter);

        if (filteredApps.size() == 1) {
            boolean autoStartApp = (boolean) SharedPreferencesHelper.getData(getContext(), "AppDrawerAutoStartApp", true);
            if(autoStartApp){
                launchApp(filteredApps.get(0));
            }
        }
    }

    private void launchApp(AppListItem app) {

        Intent launchIntent = manager.getLaunchIntentForPackage(app.label.toString());
        if (launchIntent != null) {
            boolean isPaused = isAppPaused(app);

            if (isPaused) {
                // Show a toast indicating that the app is paused
                boolean temporaryAccess = (boolean) SharedPreferencesHelper.getData(getContext(), "AppPauseControlsTemporaryAccess", false);
                if(!temporaryAccess){
                    Toast.makeText(getContext(), app.name + " is paused", Toast.LENGTH_SHORT).show();
                }
                else{
                    View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_temporary_pause_app, null);

                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
                    bottomSheetDialog.setContentView(bottomSheetView);

                    Button buttonDismiss = bottomSheetView.findViewById(R.id.buttonDismiss);
                    TextView buttonUnpauseFor5Min = bottomSheetView.findViewById(R.id.buttonUnpauseFor5Min);

                    buttonDismiss.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bottomSheetDialog.dismiss();
                        }
                    });

                    buttonUnpauseFor5Min.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PausedApps newPausedApps = new PausedApps();

                            long currentDateTime = System.currentTimeMillis();
                            long currentDateTimePlusFive = currentDateTime + (5 * 60 * 1000);

                            PausedApps pausedAppsData = database.mainDAO().getSingleApp(app.label.toString());


                            newPausedApps.setPackageName(app.label.toString());
                            newPausedApps.setPausedStartTime(String.valueOf(currentDateTimePlusFive));
                            newPausedApps.setPausedEndTime(pausedAppsData.getPausedEndTime());
                            database.mainDAO().insert(newPausedApps);


                            Toast.makeText(getContext(), app.name + " paused for an additional 5 minutes", Toast.LENGTH_SHORT).show();

                            // Dismiss the dialog after updating the pause duration
                            bottomSheetDialog.dismiss();
                            onResume();
                        }
                    });

                    bottomSheetDialog.show();

                }

            } else {
                InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                searchEditText.clearFocus();
                searchEditText.setText("");
                startActivity(launchIntent);
            }
        }
    }

    private boolean isAppPaused(AppDrawerFragment.AppListItem app) {
        long currentTimeMillis = System.currentTimeMillis();
        for (PausedApps pausedApp : pausedAppsList) {
            long startTimeMillis = Long.parseLong(pausedApp.getPausedStartTime());
            long endTimeMillis = Long.parseLong(pausedApp.getPausedEndTime());

            if (currentTimeMillis >= startTimeMillis && currentTimeMillis <= endTimeMillis &&
                    app.label.toString().equals(pausedApp.getPackageName())) {
                return true; // App is within paused time range
            }
        }
        return false; // App is not paused
    }

    private void showCustomDialog(AppDrawerFragment.AppListItem app) {
        // Inflate the custom dialog layout
        boolean isPaused = isAppPaused(app);

        if (isPaused) {
            // Show a toast indicating that the app is paused
            Toast.makeText(getContext(), app.name + " is paused", Toast.LENGTH_SHORT).show();
            return;
        }

        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_app_info, null);

        ImageView imageViewAppIcon = bottomSheetView.findViewById(R.id.imageViewAppIcon);
        TextView textViewAppName = bottomSheetView.findViewById(R.id.textViewAppName);
        ImageButton buttonAppInfo = bottomSheetView.findViewById(R.id.buttonAppInfo);
        ImageButton buttonUninstall = bottomSheetView.findViewById(R.id.buttonUninstall);

        Button buttonPauseFor1Hour = bottomSheetView.findViewById(R.id.buttonPauseFor1Hour);
        Button buttonPauseForTheDay = bottomSheetView.findViewById(R.id.buttonPauseForTheDay);



        database = RoomDB.getInstance(getContext());
        pausedAppsList = database.mainDAO().getAll();

        // Set app information in the dialog views
        imageViewAppIcon.setImageDrawable((Drawable) app.icon);
        textViewAppName.setText(app.name);

        // Create a BottomSheetDialog instance
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(bottomSheetView);


        // Set the click listener for the "App Info" button
        buttonAppInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + app.label.toString()));
                startActivity(intent);

                bottomSheetDialog.dismiss(); // Dismiss the dialog after opening app settings
            }
        });

        buttonUninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to uninstall the app
                Intent intent = new Intent(Intent.ACTION_DELETE);
                intent.setData(Uri.parse("package:" + app.label.toString()));
                startActivity(intent);

                bottomSheetDialog.dismiss(); // Dismiss the dialog after initiating uninstallation
            }
        });


        buttonPauseFor1Hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PausedApps newPausedApps = new PausedApps();


                long currentTimeMillis = System.currentTimeMillis();
                String currentDateTime = String.valueOf(currentTimeMillis);

                long oneHourInMillis = 60 * 60 * 1000;
                long pausedEndTimeMillis = currentTimeMillis + oneHourInMillis;
                String pausedEndDateTime = String.valueOf(pausedEndTimeMillis);

                newPausedApps.setPackageName(app.label.toString());
                newPausedApps.setPausedStartTime(currentDateTime);
                newPausedApps.setPausedEndTime(pausedEndDateTime);

                database.mainDAO().insert(newPausedApps);

                Toast.makeText(getContext(), app.name + " has been paused for 1 hour", Toast.LENGTH_SHORT).show();
//                printDataInDialog(pausedAppsList);

                bottomSheetDialog.dismiss(); // Dismiss the dialog after initiating uninstallation
                onResume();



            }
        });


        buttonPauseForTheDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PausedApps newPausedApps = new PausedApps();


                // Get the current date and time
                long currentTimeMillis = System.currentTimeMillis();
                String currentDateTime = String.valueOf(currentTimeMillis);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(currentTimeMillis);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);

                long endOfDayInMillis = calendar.getTimeInMillis();
                String endOfDayDateTime = String.valueOf(endOfDayInMillis);

                newPausedApps.setPackageName(app.label.toString());
                newPausedApps.setPausedStartTime(currentDateTime);
                newPausedApps.setPausedEndTime(endOfDayDateTime);

                database.mainDAO().insert(newPausedApps);

                Toast.makeText(getContext(), app.name + " has been paused for the day", Toast.LENGTH_SHORT).show();
//                printDataInDialog(pausedAppsList);

                bottomSheetDialog.dismiss();
                onResume();


            }
        });


        bottomSheetDialog.show();
    }


    public void printDataInDialog(List<PausedApps> pausedAppsList) {
        StringBuilder stringBuilder = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (PausedApps pausedApps : pausedAppsList) {
            stringBuilder.append("Package Name: ").append(pausedApps.getPackageName()).append("\n");

            long startTimeMillis = Long.parseLong(pausedApps.getPausedStartTime());
            String startTime = sdf.format(new Date(startTimeMillis));
            stringBuilder.append("Paused Start Time: ").append(startTime).append("\n");

            long endTimeMillis = Long.parseLong(pausedApps.getPausedEndTime());
            String endTime = sdf.format(new Date(endTimeMillis));
            stringBuilder.append("Paused End Time: ").append(endTime).append("\n");

            stringBuilder.append("\n");
        }

        String allData = stringBuilder.toString();

        // Create a dialog to display the formatted data
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Paused Apps Information");
        builder.setMessage(allData);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do something when "OK" is clicked, if needed
            }
        });
        builder.setCancelable(true); // Allow dismissal when touching outside of the dialog

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private class AppAdapter extends RecyclerView.Adapter<AppDrawerFragment.AppAdapter.AppViewHolder> {

        private List<AppDrawerFragment.AppListItem> appsList;
        private List<PausedApps> pausedAppsList; // New field for paused apps

        public AppAdapter(List<AppListItem> appsList, List<PausedApps> pausedAppsList) {
            this.appsList = appsList;
            this.pausedAppsList = pausedAppsList;
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

            boolean isPaused = isAppPaused(app);

            // Set text color based on the app's paused status
            if (isPaused) {
                holder.appName.setTextColor(ContextCompat.getColor(getContext(), R.color.SecondaryTextColor));
                holder.imageViewTimer.setVisibility(View.VISIBLE);
            }
            else{
                holder.appName.setTextColor(ContextCompat.getColor(getContext(), R.color.PrimaryTextColor));
                holder.imageViewTimer.setVisibility(View.GONE);
            }

            if (app.showIcon) {
                holder.appIcon.setVisibility(View.VISIBLE);
                holder.appIcon.setImageDrawable((Drawable) app.icon);
            } else {
                holder.appIcon.setVisibility(View.GONE);
            }

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

        public void updateAppIconVisibility(boolean showIcons) {
            for (AppListItem app : appsList) {
                app.showIcon = showIcons;
            }
            notifyDataSetChanged();
        }

        private boolean isAppPaused(AppListItem app) {
            long currentTimeMillis = System.currentTimeMillis();
            for (PausedApps pausedApp : pausedAppsList) {
                long startTimeMillis = Long.parseLong(pausedApp.getPausedStartTime());
                long endTimeMillis = Long.parseLong(pausedApp.getPausedEndTime());

                if (currentTimeMillis >= startTimeMillis && currentTimeMillis <= endTimeMillis &&
                        app.label.toString().equals(pausedApp.getPackageName())) {
                    return true; // App is within paused time range
                }
            }
            return false; // App is not paused
        }

        @Override
        public int getItemCount() {
            return appsList.size();
        }

        class AppViewHolder extends RecyclerView.ViewHolder {

            TextView appName;
            ImageView appIcon;
            ImageView imageViewTimer;

            public AppViewHolder(@NonNull View itemView) {
                super(itemView);
                appName = itemView.findViewById(R.id.name);
                appIcon = itemView.findViewById(R.id.imageViewAppLogo);
                imageViewTimer = itemView.findViewById(R.id.imageViewTimer);
            }
        }
    }

    public static class AppListItem {
        public CharSequence label;
        public CharSequence name;
        public Drawable icon;
        boolean showIcon;
    }
}
