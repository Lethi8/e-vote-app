package com.example.vota.ui;

import android.os.Bundle;

import com.example.vota.R;

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnElections).setOnClickListener(v -> open(ElectionsActivity.class));
        findViewById(R.id.btnLearn).setOnClickListener(v -> open(LearnActivity.class));
        findViewById(R.id.btnLogin).setOnClickListener(v -> open(LoginActivity.class));
    }
}