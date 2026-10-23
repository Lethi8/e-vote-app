package com.example.vota.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
//stores and applies the users theme choice and uses its own sharedpreferances file that is separate from the sessionmanager
public final class ThemeManager {
    public static final int MODE_LIGHT = 0;
    public static final int MODE_DARK = 1;
    public static final int MODE_SYSTEM = 2;

    private static final String PREFS = "vota_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";

    private ThemeManager() {}
//Reads the saved theme and defaults to the phones system if no theme was ever set
    public static int getMode(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_THEME_MODE, MODE_SYSTEM);
    }
//Saves the chosen mode and applies it fast fast
    public static void setMode(Context context, int mode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_THEME_MODE, mode).apply();
        apply(mode);
    }
//Switches the apps night mode via appcompatdelegate
    public static void apply(int mode) {
        switch (mode) {
            case MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public static void applySaved(Context context) {
        apply(getMode(context));
    }
}