package com.example.vota.util;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.vota.ui.ShimmerView;
//Drives the shimmer loading state thats shared accross the screens that use it
public final class SkeletonUtil {
    private SkeletonUtil() {}
//shows the skeleton
    public static void showSkeletonList(FrameLayout frame, LinearLayout container, ShimmerView shimmer, int layoutRes, int count) {
        container.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        for (int i = 0; i < count; i++) {
            View v = inflater.inflate(layoutRes, container, false);
            container.addView(v);
        }
        frame.setVisibility(View.VISIBLE);
        shimmer.startShimmer();
    }
//hides the skeleton and stops the animation so they dont stay in the memory
    public static void hideSkeletonList(FrameLayout frame, LinearLayout container, ShimmerView shimmer) {
        shimmer.stopShimmer();
        frame.setVisibility(View.GONE);
        container.removeAllViews();
    }
}