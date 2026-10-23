package com.example.vota;

import android.app.Application;

import com.example.vota.util.ThemeManager;
//basically a subcless that first runs on launch before any of the activities and all it does
//is apply the saved them choice with immediate effect so the theme they chose is active from the jump
public class VotaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySaved(this);
    }
}