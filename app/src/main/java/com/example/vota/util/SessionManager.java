package com.example.vota.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {
    private static final String TAG = "VOTA_SESSION";
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        // Use the application context so we never hold on to an Activity.
        Context app = context.getApplicationContext();
        SharedPreferences p;
        try {
            MasterKey key = new MasterKey.Builder(app)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            p = EncryptedSharedPreferences.create(
                    app,
                    "vota_session_v2",
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            // Catches Keystore runtime errors too, not just GeneralSecurity/IO exceptions.
            Log.e(TAG, "Encrypted prefs failed, using fallback", e);
            p = app.getSharedPreferences("vota_session_fallback", Context.MODE_PRIVATE);
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

    public void cacheEmail(String email) {
        prefs.edit().putString("email", email).apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() { return prefs.getBoolean("logged_in", false); }
    public String userId()       { return prefs.getString("user_id", ""); }
    public String accessToken()  { return prefs.getString("access_token", ""); }
    public String refreshToken() { return prefs.getString("refresh_token", ""); }
    public String name()         { return prefs.getString("name", "Voter"); }
    public String role()         { return prefs.getString("role", "voter"); }
    public boolean isVerified()  { return prefs.getBoolean("verified", false); }
    public String email()        { return prefs.getString("email", ""); }
}