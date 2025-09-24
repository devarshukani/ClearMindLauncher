package com.devarshukani.clearmindlauncher.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.devarshukani.clearmindlauncher.Animation.CustomPageTransformer;
import com.devarshukani.clearmindlauncher.CustomViews.CustomViewPager2;
import com.devarshukani.clearmindlauncher.Helper.AnimateLinearLayoutButton;
import com.devarshukani.clearmindlauncher.R;

public class SwipeFragment extends Fragment {
    private CustomViewPager2 viewPager;
    private AnimateLinearLayoutButton animHelper; // Add haptics helper

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipe, container, false);
        viewPager = view.findViewById(R.id.viewPager);

        animHelper = new AnimateLinearLayoutButton(); // Initialize haptics helper

        viewPager.setAdapter(new MyPagerAdapter(this));
        viewPager.setPageTransformer(new CustomPageTransformer());

        // Add page change callback for haptic feedback
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Provide haptic feedback when switching pages
                if (viewPager != null) {
                    animHelper.animateButtonClickWithHaptics(viewPager);
                }
            }
        });

        // first time render glitch fix
        switchToAppDrawerFragment();
        switchToHomeFragment();

        return view;
    }

    private class MyPagerAdapter extends FragmentStateAdapter {
        public MyPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new HomeFragment();
            } else {
                return new AppDrawerFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    public void switchToHomeFragment() {
        viewPager.setCurrentItem(0);
        // Haptic feedback is handled by the page change callback
    }

    public void switchToAppDrawerFragment(){
        viewPager.setCurrentItem(1);
        // Haptic feedback is handled by the page change callback
    }
}
