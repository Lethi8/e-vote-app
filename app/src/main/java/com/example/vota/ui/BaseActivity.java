package com.example.vota.ui;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vota.R;
import com.example.vota.util.SkeletonUtil;
//parent class that every screen in the app extends
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        View content = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
//Starts another activity with no extras
    protected void open(Class<?> target) {
        startActivity(new Intent(this, target));
    }
// Loads the activity_page.xml and then fills in its body and title etc.
    protected void page(String eyebrow, String title, String body) {
        setContentView(R.layout.activity_page);
        ((TextView) findViewById(R.id.txtEyebrow)).setText(eyebrow);
        ((TextView) findViewById(R.id.txtTitle)).setText(title);
        ((TextView) findViewById(R.id.txtBody)).setText(body);
    }
//labels the page templates primary button, returning it so the caller can immediately attach a click listener
    protected Button primary(String text) {
        Button b = findViewById(R.id.btnPrimary);
        b.setText(text);
        b.setVisibility(View.VISIBLE);
        return b;
    }

    protected Button secondary(String text) {
        Button b = findViewById(R.id.btnSecondary);
        b.setText(text);
        b.setVisibility(View.VISIBLE);
        return b;
    }
//Appends one read-only info card to the page template content
    protected void addSection(String title, String body) {
        LinearLayout container = findViewById(R.id.contentContainer);
        if (container == null) return;
        View v = getLayoutInflater().inflate(R.layout.item_card, container, false);
        ((TextView) v.findViewById(R.id.txtBadge)).setText("");
        ((TextView) v.findViewById(R.id.txtTitle)).setText(title);
        ((TextView) v.findViewById(R.id.txtBody)).setText(body);
        v.findViewById(R.id.txtAction).setVisibility(View.GONE);
        v.setClickable(false);
        container.addView(v);
    }

    protected void addSection(String title, String body, int progressPercentage) {
        LinearLayout container = findViewById(R.id.contentContainer);
        if (container == null) return;

        View v = getLayoutInflater().inflate(R.layout.item_card, container, false);
        ((TextView) v.findViewById(R.id.txtBadge)).setText("");
        ((TextView) v.findViewById(R.id.txtTitle)).setText(title);
        ((TextView) v.findViewById(R.id.txtBody)).setText(body);
        v.findViewById(R.id.txtAction).setVisibility(View.GONE);
        v.setClickable(false);

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);

        int heightPx = (int) (16 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                heightPx
        );

        int marginPx = (int) (10 * getResources().getDisplayMetrics().density);
        params.setMargins(0, marginPx, 0, marginPx);
        progressBar.setLayoutParams(params);

        progressBar.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.custom_progress_bar));
        progressBar.setMax(100);
        progressBar.setProgress(progressPercentage);

        if (v instanceof LinearLayout) {
            LinearLayout cardLayout = (LinearLayout) v;
            cardLayout.addView(progressBar, 2);
        }

        container.addView(v);
    }
// shows count shimmering skeleton blocks in the layout skeletonFrame
    protected void showContentSkeleton(int count) {
        FrameLayout frame = findViewById(R.id.skeletonFrame);
        LinearLayout container = findViewById(R.id.skeletonContainer);
        ShimmerView shimmer = findViewById(R.id.shimmerOverlay);
        if (frame == null || container == null || shimmer == null) return;
        SkeletonUtil.showSkeletonList(frame, container, shimmer, R.layout.item_skeleton_card, count);
    }
//The reverse of the showcontentskeleton() and should stop the shimmer and hides the area
    protected void hideContentSkeleton() {
        FrameLayout frame = findViewById(R.id.skeletonFrame);
        LinearLayout container = findViewById(R.id.skeletonContainer);
        ShimmerView shimmer = findViewById(R.id.shimmerOverlay);
        if (frame == null || container == null || shimmer == null) return;
        SkeletonUtil.hideSkeletonList(frame, container, shimmer);
    }
}