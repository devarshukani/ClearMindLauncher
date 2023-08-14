package com.devarshukani.clearmindlauncher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.provider.Settings;


public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{

    private GestureDetector gestureDetector;
    private TextView timeTextView;
    private Handler handler;
    private Runnable updateTimeRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gestureDetector = new GestureDetector(this, this);

        timeTextView = findViewById(R.id.time);

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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the callback to prevent memory leaks
        handler.removeCallbacks(updateTimeRunnable);
    }

    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (deltaX < 0) {
                // Right to left swipe detected
                Intent intent = new Intent(MainActivity.this, AppDrawerActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

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
                try {
                    @SuppressLint("WrongConstant") Object service = getSystemService("statusbar");
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



    @Override
    public boolean onDown(MotionEvent e) {return true;}
    @Override
    public void onShowPress(MotionEvent e) {}
    @Override
    public boolean onSingleTapUp(MotionEvent e) {return false;}
    @Override
    public boolean onScroll(@NonNull MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {return false;}
    @Override
    public void onLongPress(MotionEvent e) {}
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {super.onPointerCaptureChanged(hasCapture);}


    @Override
    public void onBackPressed(){
        // do nothing
    }
}