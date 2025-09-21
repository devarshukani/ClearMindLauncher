package com.devarshukani.clearmindlauncher.Helper;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;

public class AnimateLinearLayoutButton {

    public void animateButtonClick(LinearLayout button) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 0.9f, 1f, 0.9f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(100); // Set the duration of the animation
        scaleAnimation.setRepeatCount(1); // Set the number of times the animation will repeat
        scaleAnimation.setRepeatMode(Animation.REVERSE); // Set the mode of animation repetition
        button.startAnimation(scaleAnimation);
        
        // Add haptic feedback
        performHapticFeedback(button);
    }
    
    public void animateButtonClickWithHaptics(View button) {
        if (button instanceof LinearLayout) {
            animateButtonClick((LinearLayout) button);
        } else {
            // For non-LinearLayout views, just provide haptic feedback
            performHapticFeedback(button);
        }
    }
    
    private void performHapticFeedback(View view) {
        Context context = view.getContext();
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For API 29 and above, use the lightest predefined effect
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For API 26-28, use very light amplitude (1 is the minimum) with short duration
                vibrator.vibrate(VibrationEffect.createOneShot(20, 1));
            } else {
                // For older versions, use very short duration
                vibrator.vibrate(20);
            }
        }
    }
}