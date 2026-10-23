 package com.example.vota.ui;

 import android.content.Intent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.ScrollView;
 import android.widget.TextView;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

 import androidx.appcompat.app.AppCompatActivity;
 import androidx.core.content.ContextCompat;
 import androidx.core.graphics.Insets;
 import androidx.core.view.ViewCompat;
 import androidx.core.view.WindowInsetsCompat;

 import com.example.vota.R;
 import com.example.vota.util.SkeletonUtil;

// Parent class that every screen in the app extends
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        View content = findViewById(android.R.id.content);

        if (content != null) {
            ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
                Insets bars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                );

                v.setPadding(
                        bars.left,
                        bars.top,
                        bars.right,
                        bars.bottom
                );

                return insets;
            });
        }
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            Insets bars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    bars.left,
                    bars.top,
                    bars.right,
                    bars.bottom
            );

            return insets;
        });
    }

    // Starts another activity with no extras
    protected void open(Class<?> target) {
        startActivity(new Intent(this, target));
    }

    // Loads activity_page.xml and then fills in its body and title
    protected void page(String eyebrow, String title, String body) {
        setContentView(R.layout.activity_page);

        TextView eyebrowText = findViewById(R.id.txtEyebrow);
        TextView titleText = findViewById(R.id.txtTitle);
        TextView bodyText = findViewById(R.id.txtBody);

        if (eyebrowText != null) {
            eyebrowText.setText(eyebrow);
        }

        if (titleText != null) {
            titleText.setText(title);
        }

        if (bodyText != null) {
            bodyText.setText(body);
        }
    protected void page(
            String eyebrow,
            String title,
            String body
    ) {
        setContentView(R.layout.activity_page);

        TextView txtEyebrow = findViewById(R.id.txtEyebrow);
        TextView txtTitle = findViewById(R.id.txtTitle);
        TextView txtBody = findViewById(R.id.txtBody);

        txtEyebrow.setText(eyebrow);
        txtTitle.setText(title);
        txtBody.setText(body);
    }

    // Labels the page template's primary button,
    // returning it so the caller can immediately attach a click listener
    protected Button primary(String text) {
        Button b = findViewById(R.id.btnPrimary);

        if (b != null) {
            b.setText(text);
            b.setVisibility(View.VISIBLE);
        }

        return b;
        Button button = findViewById(R.id.btnPrimary);

        button.setText(text);
        button.setVisibility(View.VISIBLE);

        return button;
    }

    // Labels the page template's secondary button
    protected Button secondary(String text) {
        Button b = findViewById(R.id.btnSecondary);

        if (b != null) {
            b.setText(text);
            b.setVisibility(View.VISIBLE);
        }

        return b;
    }

    // Appends one read-only information card to the page template content
    protected void addSection(String title, String body) {
        LinearLayout container = findViewById(R.id.contentContainer);

        if (container == null) {
            return;
        }

        View v = getLayoutInflater().inflate(
        Button button = findViewById(R.id.btnSecondary);

        button.setText(text);
        button.setVisibility(View.VISIBLE);

        return button;
    }

    protected void addSection(
            String title,
            String body
    ) {
        LinearLayout container =
                findViewById(R.id.contentContainer);

        View view = getLayoutInflater().inflate(
                R.layout.item_card,
                container,
                false
        );

        TextView badge = view.findViewById(R.id.txtBadge);
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        TextView txtBody = view.findViewById(R.id.txtBody);
        TextView action = view.findViewById(R.id.txtAction);

        badge.setText("");
        txtTitle.setText(title);
        txtBody.setText(body);

        action.setVisibility(View.GONE);

        view.setClickable(false);

        container.addView(view);
    }

    protected void addActionSection(
            String title,
            String body,
            String actionText,
            View.OnClickListener listener
    ) {
        LinearLayout container =
                findViewById(R.id.contentContainer);

        View view = getLayoutInflater().inflate(
                R.layout.item_card,
                container,
                false
        );

        TextView badge = v.findViewById(R.id.txtBadge);
        TextView titleText = v.findViewById(R.id.txtTitle);
        TextView bodyText = v.findViewById(R.id.txtBody);
        TextView action = v.findViewById(R.id.txtAction);

        if (badge != null) {
            badge.setText("");
        }

        if (titleText != null) {
            titleText.setText(title);
        }

        if (bodyText != null) {
            bodyText.setText(body);
        }

        if (action != null) {
            action.setVisibility(View.GONE);
        }

        v.setClickable(false);

        container.addView(v);
        TextView badge = view.findViewById(R.id.txtBadge);
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        TextView txtBody = view.findViewById(R.id.txtBody);
        TextView action = view.findViewById(R.id.txtAction);

        badge.setText("");
        txtTitle.setText(title);
        txtBody.setText(body);

        action.setText(actionText);
        action.setVisibility(View.VISIBLE);

        view.setClickable(true);
        view.setFocusable(true);
        view.setOnClickListener(listener);

        container.addView(view);
    }

    protected void addBackSection() {
        addActionSection(
                "Return",
                "Go back to the previous screen.",
                "Go back",
                v -> finish()
        );
    }

    protected void addRefreshSection() {
        addActionSection(
                "Refresh",
                "Reload this page.",
                "Refresh",
                v -> recreate()
        );
    }

    protected void enablePageScrollbar() {
        View root = findViewById(android.R.id.content);
        ScrollView scrollView = findFirstScrollView(root);

        if (scrollView != null) {
            scrollView.setVerticalScrollBarEnabled(true);
            scrollView.setScrollbarFadingEnabled(false);
        }
    }

    private ScrollView findFirstScrollView(View view) {
        if (view instanceof ScrollView) {
            return (ScrollView) view;
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                ScrollView found = findFirstScrollView(group.getChildAt(i));

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    // Adds an information card with a progress bar
    protected void addSection(
            String title,
            String body,
            int progressPercentage
    ) {
        LinearLayout container = findViewById(R.id.contentContainer);

        if (container == null) {
            return;
        }

        View v = getLayoutInflater().inflate(
                R.layout.item_card,
                container,
                false
        );

        TextView badge = v.findViewById(R.id.txtBadge);
        TextView titleText = v.findViewById(R.id.txtTitle);
        TextView bodyText = v.findViewById(R.id.txtBody);
        TextView action = v.findViewById(R.id.txtAction);

        if (badge != null) {
            badge.setText("");
        }

        if (titleText != null) {
            titleText.setText(title);
        }

        if (bodyText != null) {
            bodyText.setText(body);
        }

        if (action != null) {
            action.setVisibility(View.GONE);
        }

        v.setClickable(false);

        ProgressBar progressBar = new ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
        );

        int heightPx = (int) (
                16 * getResources().getDisplayMetrics().density
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        heightPx
                );

        int marginPx = (int) (
                10 * getResources().getDisplayMetrics().density
        );

        params.setMargins(
                0,
                marginPx,
                0,
                marginPx
        );

        progressBar.setLayoutParams(params);

        progressBar.setProgressDrawable(
                ContextCompat.getDrawable(
                        this,
                        R.drawable.custom_progress_bar
                )
        );

        progressBar.setMax(100);

        // Keep progress between 0 and 100
        if (progressPercentage < 0) {
            progressPercentage = 0;
        }

        if (progressPercentage > 100) {
            progressPercentage = 100;
        }

        progressBar.setProgress(progressPercentage);

        if (v instanceof LinearLayout) {
            LinearLayout cardLayout = (LinearLayout) v;

            cardLayout.addView(
                    progressBar,
                    2
            );
        }

        container.addView(v);
    }

    /*
     * Adds an information card with an action button/link.
     *
     * This is used by:
     * AdminElectionsActivity
     * AdminPartiesActivity
     */
    protected void addActionSection(
            String title,
            String body,
            String actionText,
            View.OnClickListener listener
    ) {
        LinearLayout container = findViewById(R.id.contentContainer);

        if (container == null) {
            return;
        }

        View v = getLayoutInflater().inflate(
                R.layout.item_card,
                container,
                false
        );

        TextView badge = v.findViewById(R.id.txtBadge);
        TextView titleText = v.findViewById(R.id.txtTitle);
        TextView bodyText = v.findViewById(R.id.txtBody);
        TextView action = v.findViewById(R.id.txtAction);

        if (badge != null) {
            badge.setText("");
        }

        if (titleText != null) {
            titleText.setText(title);
        }

        if (bodyText != null) {
            bodyText.setText(body);
        }

        if (action != null) {
            action.setText(actionText);
            action.setVisibility(View.VISIBLE);

            if (listener != null) {
                action.setOnClickListener(listener);
            }
        }

        v.setClickable(false);

        container.addView(v);
    }

    /*
     * Adds a Back action to the bottom of the page.
     * Used by:
     * AdminElectionsActivity
     * AdminPartiesActivity
     * AuditLogsActivity
     */
    protected void addBackSection() {
        LinearLayout container = findViewById(R.id.contentContainer);

        if (container == null) {
            return;
        }

        View v = getLayoutInflater().inflate(
                R.layout.item_card,
                container,
                false
        );

        TextView badge = v.findViewById(R.id.txtBadge);
        TextView titleText = v.findViewById(R.id.txtTitle);
        TextView bodyText = v.findViewById(R.id.txtBody);
        TextView action = v.findViewById(R.id.txtAction);

        if (badge != null) {
            badge.setText("");
        }

        if (titleText != null) {
            titleText.setText("Navigation");
        }

        if (bodyText != null) {
            bodyText.setText("Return to the previous page.");
        }

        if (action != null) {
            action.setText("Back");
            action.setVisibility(View.VISIBLE);

            action.setOnClickListener(view -> finish());
        }

        v.setClickable(false);

        container.addView(v);
    }

    /*
     * Enables the vertical scrollbar on activity_page.xml.
     *
     * activity_page.xml currently uses a ScrollView without an ID,
     * so we obtain the first child of android.R.id.content and
     * check whether it is a ScrollView.
     */
    protected void enablePageScrollbar() {
        View root = findViewById(android.R.id.content);

        if (root == null) {
            return;
        }

        if (root instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) root;

            scrollView.setVerticalScrollBarEnabled(true);
            scrollView.setScrollbarFadingEnabled(false);
            scrollView.setFillViewport(true);

            return;
        }

        if (root instanceof ViewGroup) {
            ViewGroup contentGroup = (ViewGroup) root;

            for (int i = 0; i < contentGroup.getChildCount(); i++) {
                View child = contentGroup.getChildAt(i);

                if (child instanceof ScrollView) {
                    ScrollView scrollView = (ScrollView) child;

                    scrollView.setVerticalScrollBarEnabled(true);
                    scrollView.setScrollbarFadingEnabled(false);
                    scrollView.setFillViewport(true);

                    return;
                }
            }
        }
    }

    // Shows count shimmering skeleton blocks in the layout skeletonFrame
    protected void showContentSkeleton(int count) {
        FrameLayout frame = findViewById(R.id.skeletonFrame);
        LinearLayout container = findViewById(R.id.skeletonContainer);
        ShimmerView shimmer = findViewById(R.id.shimmerOverlay);

        if (frame == null || container == null || shimmer == null) {
            return;
        }

        SkeletonUtil.showSkeletonList(
                frame,
                container,
                shimmer,
                R.layout.item_skeleton_card,
                count
        );
    }

    // The reverse of showContentSkeleton()
    // Stops the shimmer and hides the skeleton area
    protected void hideContentSkeleton() {
        FrameLayout frame = findViewById(R.id.skeletonFrame);
        LinearLayout container = findViewById(R.id.skeletonContainer);
        ShimmerView shimmer = findViewById(R.id.shimmerOverlay);

        if (frame == null || container == null || shimmer == null) {
            return;
        }

        SkeletonUtil.hideSkeletonList(
                frame,
                container,
                shimmer
        );
    }
}
