package com.devarshukani.clearmindlauncher.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.devarshukani.clearmindlauncher.Adapter.AppListAdapter;
import com.devarshukani.clearmindlauncher.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements GestureDetector.OnGestureListener{

    private GestureDetector gestureDetector;
    private TextView timeTextView;
    private Handler handler;
    private Runnable updateTimeRunnable;

    private RecyclerView favouriteAppsRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        gestureDetector = new GestureDetector(getContext(), this);

        timeTextView = view.findViewById(R.id.time);

        // Initialize the Handler
        handler = new Handler(Looper.getMainLooper());

        // Create a Runnable to update time periodically
        updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                // Get the current time using system time
                long currentTimeMillis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
                String currentTime = sdf.format(new Date(currentTimeMillis));

                // Set the formatted time to the TextView
                timeTextView.setText(currentTime);

                // Schedule the Runnable to run again after a delay (e.g., every second)
                handler.postDelayed(this, 1000);
            }
        };

        // Start updating the time
        handler.post(updateTimeRunnable);



        // favourite apps section code
        List<AppDrawerFragment.AppListItem> selectedApps = retrieveSelectedAppsFromSharedPreferences();

        favouriteAppsRecyclerView = view.findViewById(R.id.favourite_apps_recycler_view);
        AppAdapter adapter = new AppAdapter(selectedApps);
        favouriteAppsRecyclerView.setAdapter(adapter);
        favouriteAppsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();


        // Retrieve the updated list of selected apps from SharedPreferences
        List<AppDrawerFragment.AppListItem> selectedApps = retrieveSelectedAppsFromSharedPreferences();

        // Update the data in the adapter and notify it of the data change
        AppAdapter adapter = (AppAdapter) favouriteAppsRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.updateData(selectedApps);
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
    public void onDestroy() {
        super.onDestroy();
        // Remove the callback to prevent memory leaks
        handler.removeCallbacks(updateTimeRunnable);
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
            startActivity(launchIntent);
        }


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

            public AppViewHolder(@NonNull View itemView) {
                super(itemView);
                appName = itemView.findViewById(R.id.name);
            }
        }
    }

}
