package com.devarshukani.clearmindlauncher.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;

import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.R;

public class PausedAppOverlayActivity extends Activity {

    private String blockedPackageName;
    private AnimateLinearLayoutButton animHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_paused_app_overlay);

        // Handle window insets for safe areas
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply padding to avoid status bar and navigation bar overlap
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });

        // Make this activity appear over other apps
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        animHelper = new AnimateLinearLayoutButton();
        
        // Get the blocked package name from intent
        blockedPackageName = getIntent().getStringExtra("blocked_package");
        
        setupUI();
    }

    private void setupUI() {
        TextView titleText = findViewById(R.id.overlay_title);
        TextView messageText = findViewById(R.id.overlay_message);
        Button closeButton = findViewById(R.id.btn_close_app);

        // Set title
        titleText.setText("Time for focus ✨");

        // Get app name for the message
        String appName = getAppName(blockedPackageName);
        String message = String.format("You've paused %s to give yourself space. It'll still be here later — right now, protect your focus and energy.", appName);
        messageText.setText(message);

        // Set up close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animHelper.animateButtonClickWithHaptics(v);
                closeApp();
            }
        });
    }

    private String getAppName(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return pm.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName; // Fallback to package name if app name not found
        }
    }

    private void closeApp() {
        // Go back to home screen
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        
        // Close this overlay
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Force close the blocked app
        if (blockedPackageName != null) {
            forceCloseApp(blockedPackageName);
        }
    }

    private void forceCloseApp(String packageName) {
        // This will bring the launcher to front, effectively "closing" the blocked app
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    @Override
    public void onBackPressed() {
        // Prevent back button from dismissing overlay
        closeApp();
    }
}