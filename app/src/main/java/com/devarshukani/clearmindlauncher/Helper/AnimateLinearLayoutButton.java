package com.devarshukani.clearmindlauncher.Helper;

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
    }
}
