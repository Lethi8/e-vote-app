package com.example.vota.ui;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    protected void open(Class<?> target) {
        startActivity(new Intent(this, target));
    }

    protected void page(String eyebrow, String title, String body) {
        setContentView(R.layout.activity_page);
        ((TextView) findViewById(R.id.txtEyebrow)).setText(eyebrow);
        ((TextView) findViewById(R.id.txtTitle)).setText(title);
        ((TextView) findViewById(R.id.txtBody)).setText(body);
    }

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
}