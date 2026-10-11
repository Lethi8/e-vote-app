package com.example.vota;

import android.app.Application;

import com.example.vota.util.ThemeManager;

public class VotaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySaved(this);
    }
}