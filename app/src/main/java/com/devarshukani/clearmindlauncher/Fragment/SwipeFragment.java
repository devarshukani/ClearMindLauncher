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
import com.devarshukani.clearmindlauncher.R;

public class SwipeFragment extends Fragment {
    private ViewPager2 viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_swipe, container, false);
        viewPager = view.findViewById(R.id.viewPager);

        viewPager.setAdapter(new MyPagerAdapter(this)); // Pass 'this' which refers to the parent Fragment
        viewPager.setPageTransformer(new CustomPageTransformer());

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
    }

    public void switchToAppDrawerFragment(){
        viewPager.setCurrentItem(1);
    }

}
