package com.example.vota.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.vota.R;
import com.example.vota.util.SessionManager;

public class HomeActivity extends BaseActivity {
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            open(LoginActivity.class);
            finish();
            return;
        }
        setContentView(R.layout.activity_home);
        ((TextView) findViewById(R.id.txtWelcome)).setText("Your voting hub, " + session.name());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });
        findViewById(R.id.btnVerify).setOnClickListener(v -> open(VerificationActivity.class));
        findViewById(R.id.btnElections).setOnClickListener(v -> open(ElectionsActivity.class));
        findViewById(R.id.btnParties).setOnClickListener(v -> open(PartyExplorerActivity.class));
        findViewById(R.id.btnLearn).setOnClickListener(v -> open(LearnActivity.class));
        findViewById(R.id.btnResults).setOnClickListener(v -> open(ResultsActivity.class));
        findViewById(R.id.btnNotifications).setOnClickListener(v -> open(NotificationsActivity.class));
        findViewById(R.id.btnBadges).setOnClickListener(v -> open(BadgesActivity.class));
        findViewById(R.id.btnProfile).setOnClickListener(v -> open(ProfileActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session != null && findViewById(R.id.txtVerified) != null) {
            ((TextView) findViewById(R.id.txtVerified)).setText(session.isVerified()
                    ? "Identity verified\n Voter account created\n○ Choose an open ballot"
                    : "○ Identity verification pending\n Voter account created\n Ballot not yet cast");
        }
    }
}