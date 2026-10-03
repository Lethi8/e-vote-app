package com.example.vota.ui;


import android.os.Bundle;

public class NotificationsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page("UPDATES", "Notifications", "Messages are private to the signed-in user.");
        addSection("Election open",
                "The 2026 Tshwane Municipal Election is open. Review candidates before voting.");
        addSection("Protect your account",
                "Vota staff will never ask for your password or ballot selection.");
        addSection("New voter guide",
                "Read the guide on recognising election misinformation.");
    }
}