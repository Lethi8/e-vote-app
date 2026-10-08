package com.example.vota.ui;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vota.R;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        View content = findViewById(android.R.id.content);

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

    protected void open(Class<?> target) {
        startActivity(new Intent(this, target));
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

    protected Button primary(String text) {
        Button button = findViewById(R.id.btnPrimary);

        button.setText(text);
        button.setVisibility(View.VISIBLE);

        return button;
    }

    protected Button secondary(String text) {
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
}
