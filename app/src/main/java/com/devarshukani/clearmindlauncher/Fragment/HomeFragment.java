package com.devarshukani.clearmindlauncher.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devarshukani.clearmindlauncher.Database.PausedApps;
import com.devarshukani.clearmindlauncher.Database.RoomDB;
import com.devarshukani.clearmindlauncher.Helper.SharedPreferencesHelper;
import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.R;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements GestureDetector.OnGestureListener{

    private GestureDetector gestureDetector;

    private RecyclerView favouriteAppsRecyclerView;
    LinearLayout ButtonDefaultLauncherHomePage;

    List<PausedApps> pausedAppsList;
    RoomDB database;
    private AnimateLinearLayoutButton animHelper; // Add haptics helper

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        database = RoomDB.getInstance(getContext());
        pausedAppsList = database.mainDAO().getAll();
        animHelper = new AnimateLinearLayoutButton(); // Initialize haptics helper

        gestureDetector = new GestureDetector(getContext(), this);

        setClock(view);

        // favourite apps section code
        List<AppDrawerFragment.AppListItem> selectedApps = retrieveSelectedAppsFromSharedPreferences();

        favouriteAppsRecyclerView = view.findViewById(R.id.favourite_apps_recycler_view);
        AppAdapter adapter = new AppAdapter(selectedApps);
        favouriteAppsRecyclerView.setAdapter(adapter);
        favouriteAppsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ButtonDefaultLauncherHomePage = view.findViewById(R.id.ButtonDefaultLauncherHomePage);

        ButtonDefaultLauncherHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClick(ButtonDefaultLauncherHomePage); // Add haptic feedback
                Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                startActivity(intent);
            }
        });

        return view;
    }

    private void setClock(View view){
        FrameLayout clockContainer = view.findViewById(R.id.clockContainer);
        clockContainer.removeAllViews();

        int displayClock = (int) SharedPreferencesHelper.getData(getContext(),"SelectedClockFaceNumber", 1);


        int layoutResId = getClockLayout(displayClock);
        View clockFace = getLayoutInflater().inflate(layoutResId, null);
        clockContainer.addView(clockFace);

        switch (displayClock){
            case 2:
                setClock_2(view);
                break;
            case 1: default:
                setClock_1(view);
                break;
        }
    }

    private int getClockLayout(int selectedClockFace) {
        switch (selectedClockFace) {
            case 2:
                return R.layout.clock_face_2;
            case 1:
            default:
                return R.layout.clock_face_1;
        }
    }

    private void setClock_1(View view){
        TextView clock_1_time, clock_1_date;
        clock_1_time = view.findViewById(R.id.clock_1_time);
        clock_1_date = view.findViewById(R.id.clock_1_date);
        clock_1_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClickWithHaptics(view); // Add haptic feedback
                Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                PackageManager packageManager = view.getContext().getPackageManager();

                if (intent.resolveActivity(packageManager) != null) {
                    view.getContext().startActivity(intent);
                } else {
                    // Handle the case where the clock app is not available on the device
                }
            }
        });

        // Initialize the Handler
        Handler handler = new Handler(Looper.getMainLooper());

        // Create a Runnable to update time periodically
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                // Get the current time using system time
                long currentTimeMillis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
                String currentTime = sdf.format(new Date(currentTimeMillis));

                // Set the formatted time to the TextView
                clock_1_time.setText(currentTime);

                // Schedule the Runnable to run again after a delay (e.g., every second)
                handler.postDelayed(this, 1000);
            }
        };

        // Start updating the time
        handler.post(updateTimeRunnable);

        clock_1_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClickWithHaptics(view); // Add haptic feedback
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_CALENDAR);
                PackageManager packageManager = view.getContext().getPackageManager();

                if (intent.resolveActivity(packageManager) != null) {
                    view.getContext().startActivity(intent);
                } else {
                    // Handle the case where the calendar app is not available on the device
                }
            }
        });

        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMMM", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);
        clock_1_date.setText(formattedDate);
    }

    private void setClock_2(View view){
        TextView clock_2_hour, clock_2_min, clock_2_date;

        clock_2_hour = view.findViewById(R.id.clock_2_hour);
        clock_2_min = view.findViewById(R.id.clock_2_min);
        clock_2_date = view.findViewById(R.id.clock_2_date);
        clock_2_hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClickWithHaptics(view); // Add haptic feedback
                Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                PackageManager packageManager = view.getContext().getPackageManager();

                if (intent.resolveActivity(packageManager) != null) {
                    view.getContext().startActivity(intent);
                } else {
                    // Handle the case where the clock app is not available on the device
                }
            }
        });

        clock_2_min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClickWithHaptics(view); // Add haptic feedback
                Intent intent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                PackageManager packageManager = view.getContext().getPackageManager();

                if (intent.resolveActivity(packageManager) != null) {
                    view.getContext().startActivity(intent);
                } else {
                    // Handle the case where the clock app is not available on the device
                }
            }
        });

        // Initialize the Handler
        Handler handler = new Handler(Looper.getMainLooper());

        // Create a Runnable to update time periodically
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                // Get the current time using system time
                long currentTimeMillis = System.currentTimeMillis();
                SimpleDateFormat sdfHour = new SimpleDateFormat("hh", Locale.getDefault());
                String currentHour = sdfHour.format(new Date(currentTimeMillis));

                SimpleDateFormat sdfMin = new SimpleDateFormat("mm", Locale.getDefault());
                String currentMin = sdfMin.format(new Date(currentTimeMillis));

                // Set the formatted time to the TextView
                clock_2_hour.setText(currentHour);
                clock_2_min.setText(currentMin);

                // Schedule the Runnable to run again after a delay (e.g., every second)
                handler.postDelayed(this, 1000);
            }
        };

        // Start updating the time
        handler.post(updateTimeRunnable);


        clock_2_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animHelper.animateButtonClickWithHaptics(view); // Add haptic feedback
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_CALENDAR);
                PackageManager packageManager = view.getContext().getPackageManager();

                if (intent.resolveActivity(packageManager) != null) {
                    view.getContext().startActivity(intent);
                } else {
                    // Handle the case where the calendar app is not available on the device
                }
            }
        });


        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMMM", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDate);
        clock_2_date.setText(formattedDate);
    }


    private boolean isLauncherDefault() {
        String myPackageName = getActivity().getPackageName();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = getActivity().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;
        return myPackageName.equals(currentHomePackage);
    }

    @Override
    public void onResume() {
        super.onResume();

        database = RoomDB.getInstance(getContext());
        pausedAppsList = database.mainDAO().getAll();

        List<AppDrawerFragment.AppListItem> selectedApps = retrieveSelectedAppsFromSharedPreferences();

        // Update the data in the adapter and notify it of the data change
        AppAdapter adapter = (AppAdapter) favouriteAppsRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateData(selectedApps);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // Retrieve the updated list of selected apps from SharedPreferences
        List<AppDrawerFragment.AppListItem> selectedApps = retrieveSelectedAppsFromSharedPreferences();

        // Update the data in the adapter and notify it of the data change
        AppAdapter adapter = (AppAdapter) favouriteAppsRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateData(selectedApps);
        }

        setClock(getView());

        if (!isLauncherDefault()) {
            ButtonDefaultLauncherHomePage.setVisibility(View.VISIBLE);
        }
        else{
            ButtonDefaultLauncherHomePage.setVisibility(View.GONE);
        }


    }






    private List<AppDrawerFragment.AppListItem> retrieveSelectedAppsFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences( getContext());
        String selectedAppsString = sharedPreferences.getString("selected_apps", null);
        Log.d("Debug", "SelectedAppsString: " + selectedAppsString);
        List<AppDrawerFragment.AppListItem> selectedApps = new ArrayList<>();

        if (selectedAppsString != null && !selectedAppsString.isEmpty()) {
            String[] appStrings = selectedAppsString.split(",");
            for (String appString : appStrings) {
                String[] appData = appString.split("\\|");
                if (appData.length == 2) {
                    AppDrawerFragment.AppListItem app = new AppDrawerFragment.AppListItem();
                    app.name = appData[0];
                    app.label = appData[1];
                    selectedApps.add(app);
                }
            }
        }

        return selectedApps;


//        SharedPreferences preferences = getSharedPreferences(getContext());
//        String selectedAppsJson = preferences.getString("selected_apps", null);
//
//        List<AppDrawerFragment.AppListItem> selectedApps = new ArrayList<>();
//
//        if (selectedAppsJson != null) {
//            try {
//                // Parse the JSON array of app names and package names
//                JSONArray jsonArray = new JSONArray(selectedAppsJson);
//
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    String appNameAndPackage = jsonArray.getString(i);
//                    String[] parts = appNameAndPackage.split("\\|");
//
//                    if (parts.length == 2) {
//                        AppDrawerFragment.AppListItem appItem = new AppDrawerFragment.AppListItem();
//                        appItem.name = parts[0];
//                        appItem.label = parts[1];
//
//
//                        // You can add more properties like icon if needed
//
//                        selectedApps.add(appItem);
//
//                        Log.d("DATA CHECK", appItem.name.toString());
//                        Log.d("DATA CHECK", appItem.label.toString());
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//
//        return selectedApps;
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("SelectedApps", Context.MODE_PRIVATE);
    }

    @Override
    public boolean onDown(@NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(@NonNull MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (deltaX < 0) {
                // Right to left swipe detected
//                Intent intent = new Intent(getContext(), AppDrawerActivity.class);
//                startActivity(intent);
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                return true;
            } else {
                // Left to Right swipe detected
                return true;
            }
        } else {
            if (deltaY < 0) {
                // Bottom to top swipe detected
                return true;
            } else {
                // Top to bottom swipe detected
                Toast.makeText(getContext(), "Top to bottom swiped", Toast.LENGTH_SHORT).show();
                try {
                    @SuppressLint("WrongConstant") Object service = getContext().getSystemService("statusbar");
                    Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");

                    // Determine the method to call based on Android version
                    String methodName;
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        methodName = "expand";
                    } else {
                        methodName = "expandNotificationsPanel";
                    }

                    Method method = statusBarManager.getMethod(methodName);
                    method.invoke(service);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            }
        }
        return false;
    }

    private void launchApp(AppDrawerFragment.AppListItem app) {
        PackageManager manager = getContext().getPackageManager();
        Intent launchIntent = manager.getLaunchIntentForPackage(app.label.toString());
        if (launchIntent != null) {

            boolean isPaused = isAppPaused(app);
            if (isPaused) {
                // Show a toast indicating that the app is paused
                Toast.makeText(getContext(), app.name + " is paused", Toast.LENGTH_SHORT).show();
            }
            else{
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

    private class AppAdapter extends RecyclerView.Adapter<HomeFragment.AppAdapter.AppViewHolder> {

        private List<AppDrawerFragment.AppListItem> appsList;

        public AppAdapter(List<AppDrawerFragment.AppListItem> appsList) {
            this.appsList = appsList;
        }

        @NonNull
        @Override
        public HomeFragment.AppAdapter.AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_list, parent, false);
            return new HomeFragment.AppAdapter.AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeFragment.AppAdapter.AppViewHolder holder, int position) {
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

        // Method to update the data in the adapter
        public void updateData(List<AppDrawerFragment.AppListItem> newAppsList) {
            appsList.clear();
            appsList.addAll(newAppsList);
            notifyDataSetChanged();
        }

        class AppViewHolder extends RecyclerView.ViewHolder {

            TextView appName;
            ImageView imageViewTimer;

            public AppViewHolder(@NonNull View itemView) {
                super(itemView);
                appName = itemView.findViewById(R.id.name);
                imageViewTimer = itemView.findViewById(R.id.imageViewTimer);
            }
        }
    }

}
