package com.example.vota.ui;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.vota.R;

public abstract class BaseActivity extends AppCompatActivity {

    //  Keeps content clear of the system bars on Android 15+ (edge-to-edge)
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
        View v = getLayoutInflater().inflate(R.layout.item_card, container, false);
        ((TextView) v.findViewById(R.id.txtBadge)).setText("");
        ((TextView) v.findViewById(R.id.txtTitle)).setText(title);
        ((TextView) v.findViewById(R.id.txtBody)).setText(body);
        v.findViewById(R.id.txtAction).setVisibility(View.GONE);
        v.setClickable(false);
        container.addView(v);
    }
}