package com.devarshukani.clearmindlauncher;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class CustomPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.99f;
    private static final float MIN_ALPHA = 0.7f;
    private static final int ANIMATION_DURATION = 1200; // Adjust the duration as needed for slower animation

    @Override
    public void transformPage(@NonNull View page, float position) {
        int pageWidth = page.getWidth();
        int pageHeight = page.getHeight();

        if (position < -1) { // Page is off-screen to the left
            page.setAlpha(0f);
        } else if (position <= 1) { // Page is visible on the screen
            // Apply smooth movement animation based on the 'position' value
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
            float horizontalMargin = pageWidth * (1 - scaleFactor) / 2;

            if (position < 0) { // Page is sliding to the left
                page.setTranslationX(horizontalMargin - verticalMargin / 2);
            } else { // Page is sliding to the right
                page.setTranslationX(-horizontalMargin + verticalMargin / 2);
            }

            // Smoothly move the page
            float distance = pageWidth * position;
            page.setTranslationX(distance);

            // Scale the page's content (views)
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);

            // Adjust the alpha of the page
            page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        } else { // Page is off-screen to the right
            page.setAlpha(0f);
        }

        // Set a custom duration for the animations
        page.animate().setDuration(ANIMATION_DURATION).start();
    }
}
