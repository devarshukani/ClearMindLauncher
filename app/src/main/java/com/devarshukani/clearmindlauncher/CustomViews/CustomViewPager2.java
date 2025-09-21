package com.devarshukani.clearmindlauncher.CustomViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.reflect.Method;

public class CustomViewPager2 extends FrameLayout {
    private ViewPager2 viewPager;
    private float startY = 0;
    private float startX = 0;
    private boolean isTrackingVerticalSwipe = false;
    private static final int MIN_SWIPE_DISTANCE = 200;
    private static final int MIN_MOVE_DISTANCE = 80;

    public CustomViewPager2(@NonNull Context context) {
        super(context);
        init();
    }

    public CustomViewPager2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomViewPager2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        viewPager = new ViewPager2(getContext());
        viewPager.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        addView(viewPager);
    }

    // Delegate ViewPager2 methods
    public void setAdapter(androidx.recyclerview.widget.RecyclerView.Adapter adapter) {
        viewPager.setAdapter(adapter);
    }

    public void setPageTransformer(@Nullable ViewPager2.PageTransformer transformer) {
        viewPager.setPageTransformer(transformer);
    }

    public void setCurrentItem(int item) {
        viewPager.setCurrentItem(item);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        viewPager.setCurrentItem(item, smoothScroll);
    }

    public int getCurrentItem() {
        return viewPager.getCurrentItem();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Only allow swipe-down gesture on HomeFragment (position 0)
        if (viewPager.getCurrentItem() != 0) {
            return super.onInterceptTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = ev.getY();
                startX = ev.getX();
                isTrackingVerticalSwipe = false;
                break;

            case MotionEvent.ACTION_MOVE:
                float currentY = ev.getY();
                float currentX = ev.getX();
                float deltaY = currentY - startY;
                float deltaX = currentX - startX;

                // Check if this is primarily a vertical downward movement
                if (deltaY > MIN_MOVE_DISTANCE && Math.abs(deltaY) > Math.abs(deltaX) * 1.5) {
                    isTrackingVerticalSwipe = true;
                    return true; // Intercept the touch event
                }
                break;
        }

        // If we're tracking a vertical swipe, intercept it
        if (isTrackingVerticalSwipe) {
            return true;
        }

        // Otherwise, let ViewPager2 handle it normally
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isTrackingVerticalSwipe) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float deltaY = event.getY() - startY;
                    float deltaX = event.getX() - startX;

                    // Continue tracking vertical swipe
                    if (deltaY > MIN_SWIPE_DISTANCE && deltaY > Math.abs(deltaX) * 1.2) {
                        expandQuickSettings();
                        isTrackingVerticalSwipe = false;
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float finalDeltaY = event.getY() - startY;
                    float finalDeltaX = event.getX() - startX;

                    // Final check for swipe-down gesture
                    if (finalDeltaY > MIN_SWIPE_DISTANCE && finalDeltaY > Math.abs(finalDeltaX) * 1.2) {
                        expandQuickSettings();
                    }

                    isTrackingVerticalSwipe = false;
                    return true;
            }
            return true; // Continue consuming the event
        }

        // Let ViewPager2 handle horizontal swipes normally
        return super.onTouchEvent(event);
    }

    private void expandQuickSettings() {
        try {
            @SuppressLint("WrongConstant")
            Object service = getContext().getSystemService("statusbar");
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");

            // Use expandNotificationsPanel for short quick settings (single swipe equivalent)
            Method method = statusBarManager.getMethod("expandNotificationsPanel");
            method.invoke(service);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback for older Android versions
            try {
                @SuppressLint("WrongConstant")
                Object service = getContext().getSystemService("statusbar");
                Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
                Method method = statusBarManager.getMethod("expand");
                method.invoke(service);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
