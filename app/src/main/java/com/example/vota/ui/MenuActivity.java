
package com.example.vota.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.vota.R;
import com.example.vota.util.SessionManager;

public class MenuActivity extends BaseActivity {

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

        setContentView(R.layout.activity_menu);

        TextView welcome = findViewById(R.id.txtMenuWelcome);
        welcome.setText("Welcome, " + session.name());

        // CAST YOUR VOTE
        // Uses the existing election and ballot screens.
        findViewById(R.id.btnMenuVote)
                .setOnClickListener(v ->
                        open(ElectionsActivity.class));

        // VOTING HUB
        findViewById(R.id.btnMenuHome)
                .setOnClickListener(v ->
                        open(HomeActivity.class));

        // POLITICAL PARTIES
        findViewById(R.id.btnMenuParties)
                .setOnClickListener(v ->
                        open(PartyExplorerActivity.class));

        // VOTER EDUCATION
        findViewById(R.id.btnMenuLearn)
                .setOnClickListener(v ->
                        open(LearnActivity.class));

        // ELECTION RESULTS
        findViewById(R.id.btnMenuResults)
                .setOnClickListener(v ->
                        open(ResultsActivity.class));

        // NOTIFICATIONS
        findViewById(R.id.btnMenuNotifications)
                .setOnClickListener(v ->
                        open(NotificationsActivity.class));

        // BADGES
        findViewById(R.id.btnMenuBadges)
                .setOnClickListener(v ->
                        open(BadgesActivity.class));

        // VERIFY IDENTITY
        findViewById(R.id.btnMenuVerify)
                .setOnClickListener(v ->
                        open(VerificationActivity.class));

        // PROFILE AND SECURITY
        findViewById(R.id.btnMenuProfile)
                .setOnClickListener(v ->
                        open(ProfileActivity.class));

        // ADMIN DASHBOARD
        View adminButton = findViewById(R.id.btnMenuAdmin);

        adminButton.setVisibility(
                "admin".equalsIgnoreCase(session.role())
                        ? View.VISIBLE
                        : View.GONE
        );

        adminButton.setOnClickListener(v -> {
            if ("admin".equalsIgnoreCase(session.role())) {
                open(AdminActivity.class);
            }
        });

        // SIGN OUT
        findViewById(R.id.btnMenuLogout)
                .setOnClickListener(v -> {

                    session.logout();

                    Intent intent = new Intent(
                            this,
                            MainActivity.class
                    );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    startActivity(intent);
                    finish();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (session != null) {
            View adminButton = findViewById(R.id.btnMenuAdmin);

            if (adminButton != null) {
                adminButton.setVisibility(
                        "admin".equalsIgnoreCase(session.role())
                                ? View.VISIBLE
                                : View.GONE
                );
            }
        }
    }
}
