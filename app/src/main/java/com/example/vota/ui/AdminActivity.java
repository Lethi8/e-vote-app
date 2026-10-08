package com.example.vota.ui;

import android.os.Bundle;
import android.widget.Toast;

import com.example.vota.util.SessionManager;

public class AdminActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);

        // Only admins may use this screen
        if (!"admin".equalsIgnoreCase(sessionManager.role())) {
            Toast.makeText(
                    this,
                    "Admin access required",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        page(
                "ROLE: ADMIN",
                "Admin dashboard",
                "Manage elections, political parties and audit logs."
        );

        // BUTTON 1
        primary("Manage elections")
                .setOnClickListener(v ->
                        open(AdminElectionsActivity.class)
                );

        // BUTTON 2
        secondary("Manage parties")
                .setOnClickListener(v ->
                        open(AdminPartiesActivity.class)
                );

        // AUDIT LOGS
        addActionSection(
                "Audit logs",
                "Review administrator activity recorded by the system.",
                "View audit logs",
                v -> open(AuditLogsActivity.class)
        );

        // REFRESH
        addActionSection(
                "Refresh dashboard",
                "Reload the admin dashboard.",
                "Refresh",
                v -> refreshDashboard()
        );

        // GO BACK
        addActionSection(
                "Return",
                "Go back to the previous screen.",
                "Go back",
                v -> finish()
        );
    }

    private void refreshDashboard() {
        Toast.makeText(
                this,
                "Refreshing...",
                Toast.LENGTH_SHORT
        ).show();

        recreate();
    }
}