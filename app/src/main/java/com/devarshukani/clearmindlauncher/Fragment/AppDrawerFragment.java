package com.devarshukani.clearmindlauncher.Fragment;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import android.view.MotionEvent;
import java.util.HashMap;
import java.util.Map;
import android.view.HapticFeedbackConstants;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.provider.Settings;

public class AppDrawerFragment extends Fragment{

    private static final String TAG = "AppDrawerFragment";
    // Debug flag key to force vibrator fallback for testing
    private static final String PREF_FORCE_VIBRATOR = "AppDrawerForceVibrator";

    private PackageManager manager;
    private List<AppDrawerFragment.AppListItem> apps;
    private List<SearchResultItem> searchResults; // New list for search results
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private ImageButton btnSettings;

    List<PausedApps> pausedAppsList;
    RoomDB database;
    private AnimateLinearLayoutButton animHelper; // Add helper for haptics

    // Add variables to prevent toast spam
    private String lastToastMessage = "";
    private long lastToastTime = 0;
    private static final long TOAST_COOLDOWN = 2000; // 2 seconds cooldown

    // Add variables to prevent dialog spam
    private String lastDialogAppPackage = "";
    private long lastDialogTime = 0;
    private static final long DIALOG_COOLDOWN = 3000; // 3 seconds cooldown for dialogs

    // A-Z scroll bar variables
    private LinearLayout alphabetScrollBar;
    private View alphabetTouchArea; // Add the expanded touch area
    private View alphabetOverlay1; // First overlay layer
    private View alphabetOverlay2; // Second overlay layer
    private TextView selectedLetterIndicator;
    private TextView[] letterViews;
    private String currentSelectedLetter = "";
    private boolean isScrolling = false;
    private Handler hideIndicatorHandler = new Handler(Looper.getMainLooper());
    private Map<String, Integer> letterPositions = new HashMap<>();
    // Haptics throttle: ensure we don't fire vibrations too rapidly while dragging
    private long lastHapticTime = 0L;
    private static final long HAPTIC_COOLDOWN_MS = 40L; // min 40ms between haptics

    // Search result item class
    public static class SearchResultItem {
        public enum SearchType {
            APP, GOOGLE, CHATGPT
        }

        public CharSequence name;
        public String packageName;
        public Drawable icon;
        public SearchType type;
        public String query;
        public boolean showIcon;

        public SearchResultItem(CharSequence name, String packageName, Drawable icon, SearchType type, String query, boolean showIcon) {
            this.name = name;
            this.packageName = packageName;
            this.icon = icon;
            this.type = type;
            this.query = query;
            this.showIcon = showIcon;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_drawer, container, false);

        database = RoomDB.getInstance(getContext());
        pausedAppsList = database.mainDAO().getAll();
        animHelper = new AnimateLinearLayoutButton(); // Initialize haptics helper

        searchEditText = view.findViewById(R.id.ETHomeSearchField);
        btnSettings = view.findViewById(R.id.btnSettings);
        alphabetScrollBar = view.findViewById(R.id.alphabetScrollBar);
        alphabetTouchArea = view.findViewById(R.id.alphabetTouchArea); // Initialize the touch area
        alphabetOverlay1 = view.findViewById(R.id.alphabetOverlay1); // Initialize first overlay
        alphabetOverlay2 = view.findViewById(R.id.alphabetOverlay2); // Initialize second overlay
        selectedLetterIndicator = view.findViewById(R.id.selectedLetterIndicator);

        loadApps();
        setupRecyclerView(view);
        setupSearchBar();
        setupAlphabetScrollBar();

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

        // Clear any existing filter state when fragment resumes
        currentSelectedLetter = "";
        resetLetterHighlight();

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

        // Fix BroadcastReceiver registration for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getContext().registerReceiver(appInstallReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            getContext().registerReceiver(appInstallReceiver, filter);
        }

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

        String myPackage = getContext().getPackageName();
        for (ResolveInfo ri : availableActivities) {
            // Skip the launcher app itself
            if (ri.activityInfo.packageName != null && ri.activityInfo.packageName.equals(myPackage)) {
                continue;
            }

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
        AppAdapter adapter = new AppAdapter(apps, pausedAppsList, ""); // Pass empty filter initially
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        calculateLetterPositions();
    }

    private void setupAlphabetScrollBar() {
        // Initialize letter views array
        letterViews = new TextView[26];
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int i = 0; i < 26; i++) {
            String letter = String.valueOf(alphabet.charAt(i));
            int resId = getResources().getIdentifier("letter" + letter, "id", getContext().getPackageName());
            letterViews[i] = alphabetScrollBar.findViewById(resId);

            final String finalLetter = letter;
            letterViews[i].setOnClickListener(v -> scrollToLetter(finalLetter));

            // Disable touch events on individual letters to prevent conflicts
            letterViews[i].setClickable(true);
            letterViews[i].setFocusable(false);
            letterViews[i].setFocusableInTouchMode(false);
        }

        // Primary touch listener for the top overlay layer (highest priority)
        alphabetOverlay2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isScrolling = true;
                        handleOverlayTouch(event);
                        // Disable click events on individual letters during drag
                        for (TextView letterView : letterViews) {
                            letterView.setClickable(false);
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (isScrolling) {
                            handleOverlayTouch(event);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isScrolling = false;
                        // Immediately show all apps but keep scroll position
                        showAllAppsAtCurrentPosition();
                         // Start fading the center indicator instantly on release
                        hideLetterIndicatorImmediately();
                        // Re-enable click events on individual letters
                        for (TextView letterView : letterViews) {
                            letterView.setClickable(true);
                        }
                        v.performClick();
                        return true;
                }
                return false;
            }
        });

        // Secondary touch listener for the first overlay layer
        alphabetOverlay1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isScrolling = true;
                        handleOverlayTouch(event);
                        // Disable click events on individual letters during drag
                        for (TextView letterView : letterViews) {
                            letterView.setClickable(false);
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (isScrolling) {
                            handleOverlayTouch(event);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isScrolling = false;
                        // Immediately show all apps but keep scroll position
                        showAllAppsAtCurrentPosition();
                        // Start fading the center indicator instantly on release
                        hideLetterIndicatorImmediately();
                        // Re-enable click events on individual letters
                        for (TextView letterView : letterViews) {
                            letterView.setClickable(true);
                        }
                        v.performClick();
                        return true;
                }
                return false;
            }
        });

        // Use the expanded touch area for all touch events (fallback)
        alphabetTouchArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isScrolling = true;
                        handleExpandedAlphabetTouch(event);
                        // Disable click events on individual letters during drag
                        for (TextView letterView : letterViews) {
                            letterView.setClickable(false);
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Handle drag functionality
                        if (isScrolling) {
                            handleExpandedAlphabetTouch(event);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isScrolling = false;
                        // Immediately show all apps but keep scroll position
                        showAllAppsAtCurrentPosition();
                        // Start fading the center indicator instantly on release
                        hideLetterIndicatorImmediately();
                        // Re-enable click events on individual letters
                        for (TextView letterView : letterViews) {
                            letterView.setClickable(true);
                        }
                        v.performClick();
                        return true;
                }
                return false;
            }
        });

        // Also add touch listener to the LinearLayout itself to handle direct letter touches (final fallback)
        alphabetScrollBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isScrolling = true;
                        handleDirectLetterTouch(event);
                        // Disable click events on individual letters during drag
                        for (TextView letterView : letterViews) {
                            letterView.setClickable(false);
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Handle drag functionality directly on letters
                        if (isScrolling) {
                            handleDirectLetterTouch(event);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isScrolling = false;
                        // Immediately show all apps but keep scroll position
                        showAllAppsAtCurrentPosition();
                        // Start fading the center indicator instantly on release
                        hideLetterIndicatorImmediately();
                        // Re-enable click events on individual letters
                        for (TextView letterView : letterViews) {
                            letterView.setClickable(true);
                        }
                        v.performClick();
                        return true;
                }
                return false;
            }
        });
    }

    // New method to show all apps but maintain current scroll position
    private void showAllAppsAtCurrentPosition() {
        // Get the current scroll position before changing the adapter
        int currentScrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        
        // Reset adapter to show all apps normally (no filter)
        AppAdapter adapter = new AppAdapter(apps, pausedAppsList, "");
        recyclerView.setAdapter(adapter);
        
        // Maintain the scroll position where the user stopped
        if (currentScrollPosition >= 0 && currentScrollPosition < apps.size()) {
            recyclerView.scrollToPosition(currentScrollPosition);
        }
        
        // Reset letter highlighting but don't change scroll position
        for (TextView letterView : letterViews) {
            letterView.setTextColor(ContextCompat.getColor(getContext(), R.color.SecondaryTextColor));
            letterView.setTextSize(12f);
            letterView.setScaleX(1.0f);
            letterView.setScaleY(1.0f);
        }
    }

    private void hideLetterIndicatorWithDelay() {
        hideIndicatorHandler.removeCallbacksAndMessages(null);
        hideIndicatorHandler.postDelayed(() -> {
            if (!isScrolling) {
                ObjectAnimator fadeOut = ObjectAnimator.ofFloat(selectedLetterIndicator, "alpha", 1f, 0f);
                fadeOut.setDuration(300);
                fadeOut.start();
                fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        selectedLetterIndicator.setVisibility(View.GONE);
                        // Don't reset the scroll position or filter here anymore
                        currentSelectedLetter = "";
                    }
                });
            }
        }, 1000); // Reduced delay to 1 second just for indicator hiding
    }

    // Hide the selected-letter indicator immediately (used on drag release)
    private void hideLetterIndicatorImmediately() {
        // Cancel any pending delayed hides
        hideIndicatorHandler.removeCallbacksAndMessages(null);

        if (selectedLetterIndicator == null) return;

        // If already invisible or fully transparent, ensure it's hidden
        if (selectedLetterIndicator.getVisibility() != View.VISIBLE || selectedLetterIndicator.getAlpha() == 0f) {
            selectedLetterIndicator.setVisibility(View.GONE);
            currentSelectedLetter = "";
            return;
        }

        // Fade out immediately and hide on end
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(selectedLetterIndicator, "alpha", selectedLetterIndicator.getAlpha(), 0f);
        fadeOut.setDuration(200); // shorter immediate fade for snappier feel
        fadeOut.start();
        fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                selectedLetterIndicator.setVisibility(View.GONE);
                currentSelectedLetter = "";
            }
        });
    }

    private void setupSearchBar() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());

                // Hide/show A-Z scrollbar based on search text
                toggleAlphabetScrollBar(s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Handle focus changes to move search bar above keyboard
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    animHelper.animateButtonClickWithHaptics(searchEditText);
                    // Adjust layout when keyboard appears
                    adjustLayoutForKeyboard(true);
                } else {
                    // Reset layout when keyboard disappears
                    adjustLayoutForKeyboard(false);
                }
            }
        });
    }

    private void toggleAlphabetScrollBar(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        if (alphabetScrollBar != null) {
            alphabetScrollBar.setVisibility(visibility);
        }
        if (alphabetTouchArea != null) {
            alphabetTouchArea.setVisibility(visibility);
        }
        if (alphabetOverlay1 != null) {
            alphabetOverlay1.setVisibility(visibility);
        }
        if (alphabetOverlay2 != null) {
            alphabetOverlay2.setVisibility(visibility);
        }

        // Adjust RecyclerView margin based on A-Z scrollbar visibility
        // Fix: Use RelativeLayout.LayoutParams since RecyclerView is inside RelativeLayout
        ViewGroup.LayoutParams layoutParams = recyclerView.getLayoutParams();
        if (layoutParams instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutParams;
            if (visible) {
                params.rightMargin = (int) (60 * getResources().getDisplayMetrics().density); // 60dp in px
            } else {
                params.rightMargin = 0;
            }
            recyclerView.setLayoutParams(params);
        }
    }

    private void adjustLayoutForKeyboard(boolean keyboardVisible) {
        View rootView = getView();
        if (rootView == null) return;

        if (keyboardVisible) {
            // Move search bar and content up when keyboard is visible
            rootView.post(() -> {
                // Apply bottom padding to push content above keyboard
                ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                    androidx.core.graphics.Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
                    androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    // Apply padding for system bars and IME
                    v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        Math.max(imeInsets.bottom, systemBars.bottom)
                    );

                    return insets;
                });
            });
        } else {
            // Reset to normal layout when keyboard is hidden
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Apply padding only for system bars
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

                return insets;
            });
        }
    }

    private void filterApps(String searchText) {
        boolean quickSearchEnabled = (boolean) SharedPreferencesHelper.getData(getContext(), "AppDrawerQuickSearch", false);

        if (quickSearchEnabled && !searchText.trim().isEmpty()) {
            // Quick Search mode: show apps + Google + ChatGPT search options
            searchResults = new ArrayList<>();

            // Add matching apps to search results
            for (AppDrawerFragment.AppListItem app : apps) {
                if (app.name.toString().toLowerCase().contains(searchText.toLowerCase())) {
                    SearchResultItem appResult = new SearchResultItem(
                        app.name,
                        app.label.toString(),
                        app.icon,
                        SearchResultItem.SearchType.APP,
                        searchText,
                        (boolean) SharedPreferencesHelper.getData(getContext(), "AppDrawerShowAppIcons", false)
                    );
                    searchResults.add(appResult);
                }
            }

            // Add Google search option (no icon)
            SearchResultItem googleResult = new SearchResultItem(
                    searchText,
                "com.google.search",
                null, // No icon for Google
                SearchResultItem.SearchType.GOOGLE,
                searchText,
                false // Don't show icon
            );
            searchResults.add(googleResult);

            // Add ChatGPT search option (no icon)
            SearchResultItem chatgptResult = new SearchResultItem(
                    searchText,
                "com.openai.chatgpt",
                null, // No icon for ChatGPT
                SearchResultItem.SearchType.CHATGPT,
                searchText,
                false // Don't show icon
            );
            searchResults.add(chatgptResult);

            // Use QuickSearchAdapter for Quick Search results
            QuickSearchAdapter adapter = new QuickSearchAdapter(searchResults, pausedAppsList);
            recyclerView.setAdapter(adapter);

        } else {
            // Normal search mode: only show matching apps
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
    }

    private void launchApp(AppListItem app) {

        Intent launchIntent = manager.getLaunchIntentForPackage(app.label.toString());
        if (launchIntent != null) {
            boolean isPaused = isAppPaused(app);

            if (isPaused) {
                // Show a toast indicating that the app is paused
                boolean temporaryAccess = (boolean) SharedPreferencesHelper.getData(getContext(), "AppPauseControlsTemporaryAccess", false);
                if(!temporaryAccess){
                    showToastWithCooldown(app.name + " is paused"); // Use cooldown method
                }
                else{
                    showTemporaryAccessDialogWithCooldown(app);
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

    // Helper method to show toast with spam prevention
    private void showToastWithCooldown(String message) {
        long currentTime = System.currentTimeMillis();

        // Check if enough time has passed since the last toast or if it's a different message
        if (!message.equals(lastToastMessage) || (currentTime - lastToastTime) > TOAST_COOLDOWN) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            lastToastMessage = message;
            lastToastTime = currentTime;
        }
    }

    // Helper method to show temporary access dialog with spam prevention
    private void showTemporaryAccessDialogWithCooldown(AppListItem app) {
        long currentTime = System.currentTimeMillis();

        // Check if enough time has passed since the last dialog for this app
        if (app.label.toString().equals(lastDialogAppPackage) && (currentTime - lastDialogTime) < DIALOG_COOLDOWN) {
            return; // Don't show the dialog again yet
        }

        lastDialogAppPackage = app.label.toString();
        lastDialogTime = currentTime;

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

    private void showCustomDialog(AppDrawerFragment.AppListItem app) {
        // Inflate the custom dialog layout
        boolean isPaused = isAppPaused(app);

        if (isPaused) {
            // Show a toast indicating that the app is paused
            showToastWithCooldown(app.name + " is paused"); // Use cooldown method
            return;
        }

        // Prevent dialog spam
        long currentTime = System.currentTimeMillis();
        if (app.label.toString().equals(lastDialogAppPackage) && (currentTime - lastDialogTime) < DIALOG_COOLDOWN) {
            return; // Don't show the dialog again yet
        }
        lastDialogAppPackage = app.label.toString();
        lastDialogTime = currentTime;

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
        private String filterLetter; // New field for filtering by letter

        public AppAdapter(List<AppListItem> appsList, List<PausedApps> pausedAppsList, String filterLetter) {
            this.appsList = appsList;
            this.pausedAppsList = pausedAppsList;
            this.filterLetter = filterLetter;
        }

        // Constructor for backward compatibility (search functionality)
        public AppAdapter(List<AppListItem> appsList, List<PausedApps> pausedAppsList) {
            this.appsList = appsList;
            this.pausedAppsList = pausedAppsList;
            this.filterLetter = "";
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
            boolean matchesFilter = filterLetter.isEmpty() ||
                app.name.toString().toUpperCase().startsWith(filterLetter);

            // Set text color based on multiple factors:
            // 1. If app is paused - use secondary color
            // 2. If we're filtering and app doesn't match - use faded color
            // 3. Otherwise use primary color
            if (isPaused) {
                holder.appName.setTextColor(ContextCompat.getColor(getContext(), R.color.SecondaryTextColor));
                holder.imageViewTimer.setVisibility(View.VISIBLE);
            } else if (!filterLetter.isEmpty() && !matchesFilter) {
                // Grey out apps that don't match the filter
                int fadedColor = ContextCompat.getColor(getContext(), R.color.SecondaryTextColor);
                // Make it even more faded
                fadedColor = Color.argb(100, Color.red(fadedColor), Color.green(fadedColor), Color.blue(fadedColor));
                holder.appName.setTextColor(fadedColor);
                holder.imageViewTimer.setVisibility(View.GONE);
            } else {
                holder.appName.setTextColor(ContextCompat.getColor(getContext(), R.color.PrimaryTextColor));
                holder.imageViewTimer.setVisibility(View.GONE);
            }

            // Set alpha for visual emphasis
            if (!filterLetter.isEmpty() && !matchesFilter) {
                holder.itemView.setAlpha(0.3f);
            } else {
                holder.itemView.setAlpha(1.0f);
            }

            if (app.showIcon) {
                holder.appIcon.setVisibility(View.VISIBLE);
                holder.appIcon.setImageDrawable((Drawable) app.icon);

                // Also fade the icon if needed
                if (!filterLetter.isEmpty() && !matchesFilter) {
                    holder.appIcon.setAlpha(0.3f);
                } else {
                    holder.appIcon.setAlpha(1.0f);
                }
            } else {
                holder.appIcon.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Add haptic feedback when clicking apps in app drawer
                    animHelper.animateButtonClickWithHaptics(v);
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

    // QuickSearchAdapter for handling search results with Google and ChatGPT options
    private class QuickSearchAdapter extends RecyclerView.Adapter<QuickSearchAdapter.QuickSearchViewHolder> {

        private List<SearchResultItem> searchResults;
        private List<PausedApps> pausedAppsList;

        public QuickSearchAdapter(List<SearchResultItem> searchResults, List<PausedApps> pausedAppsList) {
            this.searchResults = searchResults;
            this.pausedAppsList = pausedAppsList;
        }

        @NonNull
        @Override
        public QuickSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quick_search, parent, false);
            return new QuickSearchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuickSearchViewHolder holder, int position) {
            SearchResultItem searchResult = searchResults.get(position);

            holder.textViewName.setText(searchResult.name);

            // Set the appropriate type indicator
            switch (searchResult.type) {
                case APP:
                    holder.textViewType.setText("App");
                    // Check if app is paused for apps
                    boolean isPaused = isAppPausedByPackage(searchResult.packageName);
                    if (isPaused) {
                        holder.imageViewTimer.setVisibility(View.VISIBLE);
                        holder.textViewName.setTextColor(ContextCompat.getColor(getContext(), R.color.SecondaryTextColor));
                    } else {
                        holder.imageViewTimer.setVisibility(View.GONE);
                        holder.textViewName.setTextColor(ContextCompat.getColor(getContext(), R.color.PrimaryTextColor));
                    }
                    break;
                case GOOGLE:
                    holder.textViewType.setText("Google");
                    holder.imageViewTimer.setVisibility(View.GONE);
                    holder.textViewName.setTextColor(ContextCompat.getColor(getContext(), R.color.PrimaryTextColor));
                    break;
                case CHATGPT:
                    holder.textViewType.setText("ChatGPT");
                    holder.imageViewTimer.setVisibility(View.GONE);
                    holder.textViewName.setTextColor(ContextCompat.getColor(getContext(), R.color.PrimaryTextColor));
                    break;
            }

            // Show icon if available
            if (searchResult.showIcon && searchResult.icon != null) {
                holder.imageViewIcon.setVisibility(View.VISIBLE);
                holder.imageViewIcon.setImageDrawable(searchResult.icon);
            } else {
                holder.imageViewIcon.setVisibility(View.GONE);
            }

            // Set click listener
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Add haptic feedback when clicking search results
                    animHelper.animateButtonClickWithHaptics(v);
                    handleSearchResultClick(searchResult);
                }
            });
        }

        @Override
        public int getItemCount() {
            return searchResults.size();
        }

        class QuickSearchViewHolder extends RecyclerView.ViewHolder {
            TextView textViewName;
            TextView textViewType;
            ImageView imageViewIcon;
            ImageView imageViewTimer;

            public QuickSearchViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewName = itemView.findViewById(R.id.textViewName);
                textViewType = itemView.findViewById(R.id.textViewType);
                imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
                imageViewTimer = itemView.findViewById(R.id.imageViewTimer);
            }
        }
    }

    // Helper method to check if app is paused by package name
    private boolean isAppPausedByPackage(String packageName) {
        long currentTimeMillis = System.currentTimeMillis();
        for (PausedApps pausedApp : pausedAppsList) {
            long startTimeMillis = Long.parseLong(pausedApp.getPausedStartTime());
            long endTimeMillis = Long.parseLong(pausedApp.getPausedEndTime());

            if (currentTimeMillis >= startTimeMillis && currentTimeMillis <= endTimeMillis &&
                    packageName.equals(pausedApp.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    // Handle clicks on search results
    private void handleSearchResultClick(SearchResultItem searchResult) {
        switch (searchResult.type) {
            case APP:
                // Launch the app normally
                Intent launchIntent = manager.getLaunchIntentForPackage(searchResult.packageName);
                if (launchIntent != null) {
                    boolean isPaused = isAppPausedByPackage(searchResult.packageName);

                    if (isPaused) {
                        boolean temporaryAccess = (boolean) SharedPreferencesHelper.getData(getContext(), "AppPauseControlsTemporaryAccess", false);
                        if (!temporaryAccess) {
                            showToastWithCooldown(searchResult.name + " is paused");
                        } else {
                            // Create a temporary AppListItem for the paused app dialog
                            AppListItem tempApp = new AppListItem();
                            tempApp.name = searchResult.name;
                            tempApp.label = searchResult.packageName;
                            tempApp.icon = searchResult.icon;
                            showTemporaryAccessDialogWithCooldown(tempApp);
                        }
                    } else {
                        // Clear search and launch app
                        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                        searchEditText.clearFocus();
                        searchEditText.setText("");
                        startActivity(launchIntent);
                    }
                }
                break;

            case GOOGLE:
                // Open Google search with the query
                try {
                    String googleSearchUrl = "https://www.google.com/search?q=" + Uri.encode(searchResult.query);
                    Intent googleIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleSearchUrl));

                    // Clear search and launch Google
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                    searchEditText.clearFocus();
                    searchEditText.setText("");
                    startActivity(googleIntent);
                } catch (Exception e) {
                    showToastWithCooldown("Unable to open Google search");
                }
                break;

            case CHATGPT:
                // Try to open ChatGPT app or web version with search query
                try {

                        // Fallback to ChatGPT web version with query parameter
                        String chatgptUrl = "https://chat.openai.com/?q=" + Uri.encode(searchResult.query);
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(chatgptUrl));

                        // Clear search and launch ChatGPT web
                        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                        searchEditText.clearFocus();
                        searchEditText.setText("");
                        startActivity(webIntent);
                } catch (Exception e) {
                    showToastWithCooldown("Unable to open ChatGPT");
                }
                break;
        }
    }


    private void calculateLetterPositions() {
        letterPositions.clear();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int i = 0; i < alphabet.length(); i++) {
            String letter = String.valueOf(alphabet.charAt(i));
            int position = findFirstPositionForLetter(letter);
            letterPositions.put(letter, position);
        }
    }

    private int findFirstPositionForLetter(String letter) {
        for (int i = 0; i < apps.size(); i++) {
            String appName = apps.get(i).name.toString().toUpperCase();
            if (appName.startsWith(letter)) {
                return i;
            }
        }
        return -1; // Letter not found
    }

    private void highlightLetter(int index) {
        // Reset all letter colors and sizes
        for (int i = 0; i < letterViews.length; i++) {
            letterViews[i].setTextColor(ContextCompat.getColor(getContext(), R.color.SecondaryTextColor));
            letterViews[i].setTextSize(12f);
            letterViews[i].setScaleX(1.0f);
            letterViews[i].setScaleY(1.0f);
        }

        // Highlight selected letter with enhanced visual feedback
        if (index >= 0 && index < letterViews.length) {
            letterViews[index].setTextColor(ContextCompat.getColor(getContext(), R.color.PrimaryTextColor));
            letterViews[index].setTextSize(14f);

            // Add immediate scale for drag responsiveness
            letterViews[index].setScaleX(1.3f);
            letterViews[index].setScaleY(1.3f);

            // Quick pulse animation for better visual feedback
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(letterViews[index], "scaleX", 1.3f, 1.4f, 1.3f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(letterViews[index], "scaleY", 1.3f, 1.4f, 1.3f);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(100); // Shorter duration for drag responsiveness
            animatorSet.start();
        }
    }

    // Utility and touch handling for alphabet scroll bar

    private void resetLetterHighlight() {
        if (letterViews == null) return;
        for (TextView letterView : letterViews) {
            if (letterView == null) continue;
            letterView.setTextColor(ContextCompat.getColor(getContext(), R.color.SecondaryTextColor));
            letterView.setTextSize(12f);
            letterView.setScaleX(1.0f);
            letterView.setScaleY(1.0f);
        }
        if (selectedLetterIndicator != null) {
            selectedLetterIndicator.setAlpha(0f);
            selectedLetterIndicator.setVisibility(View.GONE);
        }
    }

    private void scrollToLetter(String letter) {
        if (letter == null || letterViews == null) return;
        currentSelectedLetter = letter;
        int index = letter.charAt(0) - 'A';
        highlightLetter(index);
        showSelectedLetterIndicator(letter);

        // Just scroll to position on click, do not change adapter filtering
        int position = findClosestPositionForLetter(letter);
        if (position >= 0) {
            recyclerView.scrollToPosition(position);
        }

        // Hide indicator shortly after tap
        hideLetterIndicatorWithDelay();
    }

    private void handleOverlayTouch(MotionEvent event) {
        if (alphabetScrollBar == null || letterViews == null) return;
        float y = event.getY();
        String letter = mapYToLetter(y, alphabetScrollBar.getHeight());
        applyLetterSelection(letter);
    }

    private void handleExpandedAlphabetTouch(MotionEvent event) {
        if (alphabetTouchArea == null || letterViews == null) return;
        float y = event.getY(); // y within touch area
        // Map proportionally to the alphabetScrollBar height
        int touchHeight = alphabetTouchArea.getHeight();
        int refHeight = alphabetScrollBar.getHeight() > 0 ? alphabetScrollBar.getHeight() : touchHeight;
        // Scale y from touch area to reference height
        float scaledY = refHeight > 0 && touchHeight > 0 ? (y * (refHeight / (float) touchHeight)) : y;
        String letter = mapYToLetter(scaledY, refHeight);
        applyLetterSelection(letter);
    }

    private void handleDirectLetterTouch(MotionEvent event) {
        if (alphabetScrollBar == null || letterViews == null) return;
        float y = event.getY();
        String letter = mapYToLetter(y, alphabetScrollBar.getHeight());
        applyLetterSelection(letter);
    }

    private String mapYToLetter(float y, int height) {
        if (height <= 0) return "A";
        int count = letterViews.length;
        float unit = height / (float) count;
        int idx = (int) (y / unit);
        if (idx < 0) idx = 0;
        if (idx >= count) idx = count - 1;
        char c = (char) ('A' + idx);
        return String.valueOf(c);
    }

    private void applyLetterSelection(String letter) {
        if (letter == null) return;
        // Only trigger haptic feedback when letter actually changes (and respect a small cooldown)
        String previous = currentSelectedLetter;
        int index = letter.charAt(0) - 'A';
        if (!letter.equals(previous)) {
            long now = System.currentTimeMillis();
            if ((now - lastHapticTime) >= HAPTIC_COOLDOWN_MS) {
                // Prefer to haptically target the visible indicator; fall back to the letter view or scroll bar
                View hapticTarget = selectedLetterIndicator != null ? selectedLetterIndicator : (letterViews != null && index >= 0 && index < letterViews.length ? letterViews[index] : alphabetScrollBar);
                try {
                    triggerHaptic(hapticTarget);
                } catch (Exception ignored) {
                    // If anything goes wrong, don't crash during drag
                }
                lastHapticTime = now;
            }
            currentSelectedLetter = letter;
        }

        highlightLetter(index);
        showSelectedLetterIndicator(letter);

        // During active drag, avoid heavy work (like recreating adapters) which can block haptics.
        // Only scroll the list to the best matching position for the letter.
        int position = findClosestPositionForLetter(letter);
        if (position >= 0) {
            recyclerView.scrollToPosition(position);
        }
    }

    // Try view-based haptics first, then fall back to direct vibrator vibration
    private void triggerHaptic(View target) {
        boolean performed = false;
        Log.d(TAG, "triggerHaptic called for target=" + (target != null ? target.getClass().getSimpleName() : "null") + " currentSelectedLetter=" + currentSelectedLetter);
        boolean forceVibrator = false;
        try {
            Boolean val = (Boolean) SharedPreferencesHelper.getData(getContext(), PREF_FORCE_VIBRATOR, false);
            forceVibrator = val != null && val;
        } catch (Exception e) {
            // ignore
        }

        // If system haptic feedback is disabled, prefer vibrator fallback
        boolean systemHapticsEnabled = true;
        try {
            Context ctx = getContext();
            if (ctx != null) {
                systemHapticsEnabled = Settings.System.getInt(ctx.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 1;
            }
        } catch (Exception e) {
            // ignore and assume enabled
            systemHapticsEnabled = true;
        }

        if (!forceVibrator && systemHapticsEnabled && target != null) {
            try {
                target.setHapticFeedbackEnabled(true);
                performed = target.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            } catch (Throwable t) {
                performed = false;
            }
            if (!performed) {
                try {
                    performed = target.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                } catch (Throwable t) {
                    performed = false;
                }
            }
        } else {
            Log.d(TAG, "Skipping view-based haptics: forceVibrator=" + forceVibrator + " systemHapticsEnabled=" + systemHapticsEnabled);
        }

        Log.d(TAG, "view-performed=" + performed + " forceVibrator=" + forceVibrator + " systemHapticsEnabled=" + systemHapticsEnabled);

        if (!performed || forceVibrator || !systemHapticsEnabled) {
            // Fallback to vibrator directly (animHelper also does this, but call here directly for reliability)
            Context ctx = getContext();
            if (ctx == null) return;
            Vibrator vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
                        Log.d(TAG, "vibrator: EFFECT_TICK called");
                    } catch (Exception e) {
                        // Some devices may not support predefined; try one-shot
                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
                        Log.d(TAG, "vibrator: one-shot fallback called");
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE));
                    Log.d(TAG, "vibrator: O one-shot called");
                } else {
                    vibrator.vibrate(10);
                    Log.d(TAG, "vibrator: legacy vibrate 10ms called");
                }
            } else {
                Log.d(TAG, "No vibrator available");
            }
        }
    }

    private void showSelectedLetterIndicator(String letter) {
        if (selectedLetterIndicator == null) return;
        selectedLetterIndicator.setText(letter);
        if (selectedLetterIndicator.getVisibility() != View.VISIBLE) {
            selectedLetterIndicator.setAlpha(0f);
            selectedLetterIndicator.setVisibility(View.VISIBLE);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(selectedLetterIndicator, "alpha", 0f, 1f);
            fadeIn.setDuration(150);
            fadeIn.start();
        }
    }

    private int findClosestPositionForLetter(String letter) {
        if (letterPositions == null || letterPositions.isEmpty()) return -1;
        Integer pos = letterPositions.get(letter);
        if (pos != null && pos >= 0) return pos;
        // Fallback: search forward then backward for the next available letter
        int startIdx = letter.charAt(0) - 'A';
        // Forward
        for (int i = startIdx + 1; i < 26; i++) {
            String l = String.valueOf((char) ('A' + i));
            Integer p = letterPositions.get(l);
            if (p != null && p >= 0) return p;
        }
        // Backward
        for (int i = startIdx - 1; i >= 0; i--) {
            String l = String.valueOf((char) ('A' + i));
            Integer p = letterPositions.get(l);
            if (p != null && p >= 0) return p;
        }
        return -1;
    }

    public static class AppListItem {
        public CharSequence label;
        public CharSequence name;
        public Drawable icon;
        boolean showIcon;
    }
}
