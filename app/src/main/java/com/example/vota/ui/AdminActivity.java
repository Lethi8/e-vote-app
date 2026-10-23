package com.example.vota.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.vota.util.SessionManager;

public class AdminActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager =
                new SessionManager(this);

        if (!"admin".equalsIgnoreCase(sessionManager.role())) {
            Toast.makeText(
                    this,
                    "Admin access required",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }
        page("ROLE: ADMIN", "Admin dashboard",
                "Administration must be enforced by backend roles and Row Level Security, not only by hiding buttons.");

        primary("Manage elections").setOnClickListener(v -> open(AdminElectionsActivity.class));
        secondary("Manage parties").setOnClickListener(v -> open(AdminPartiesActivity.class));

        addSection("Audit trail", "Review privileged actions. Ordinary voters must never read the audit log.");
        Button auditButton = new Button(this);
        auditButton.setText("View audit log");
        auditButton.setOnClickListener(v -> open(AuditLogsActivity.class));
        ((android.widget.LinearLayout) findViewById(com.example.vota.R.id.contentContainer)).addView(auditButton);

        page(
                "ROLE: ADMIN",
                "Admin dashboard",
                "Manage elections, political parties and audit logs."
        );

        // Make the admin dashboard scrollable if the content becomes long.
        enablePageScrollbar();

        // Main admin actions.
        primary("Manage elections")
                .setOnClickListener(v ->
                        open(AdminElectionsActivity.class)
                );

        secondary("Manage parties")
                .setOnClickListener(v ->
                        open(AdminPartiesActivity.class)
                );

        addActionSection(
                "Audit logs",
                "Review administrator activity recorded by the system.",
                "View audit logs",
                v -> open(AuditLogsActivity.class)
        );

        // Shared helper actions from BaseActivity.
        addRefreshSection();
        addBackSection();
    }
}
