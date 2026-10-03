package com.example.vota.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        SharedPreferences p;
        try {
            MasterKey key = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            p = EncryptedSharedPreferences.create(context, "vota_session_v2", key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            p = context.getSharedPreferences("vota_session_fallback", Context.MODE_PRIVATE);
        }
        prefs = p;
    }

    public void saveSession(String userId, String accessToken, String refreshToken) {
        prefs.edit()
                .putBoolean("logged_in", true)
                .putString("user_id", userId)
                .putString("access_token", accessToken)
                .putString("refresh_token", refreshToken)
                .apply();
    }

    public void cacheProfile(String name, String role, boolean verified) {
        prefs.edit()
                .putString("name", name)
                .putString("role", role)
                .putBoolean("verified", verified)
                .apply();
    }

    public void logout() { prefs.edit().clear().apply(); }

    public boolean isLoggedIn() { return prefs.getBoolean("logged_in", false); }
    public String userId() { return prefs.getString("user_id", ""); }
    public String accessToken() { return prefs.getString("access_token", ""); }
    public String refreshToken() { return prefs.getString("refresh_token", ""); }
    public String name() { return prefs.getString("name", "Voter"); }
    public String role() { return prefs.getString("role", "voter"); }
    public boolean isVerified() { return prefs.getBoolean("verified", false); }
    public void cacheEmail(String email) { prefs.edit().putString("email", email).apply(); }
    public String email() { return prefs.getString("email", ""); }
}