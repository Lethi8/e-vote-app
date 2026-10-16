package com.example.vota.util;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.vota.ui.ShimmerView;

public final class SkeletonUtil {
    private SkeletonUtil() {}

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

    public static void hideSkeletonList(FrameLayout frame, LinearLayout container, ShimmerView shimmer) {
        shimmer.stopShimmer();
        frame.setVisibility(View.GONE);
        container.removeAllViews();
    }
}