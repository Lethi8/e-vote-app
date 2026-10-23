package com.example.vota.ui;

import android.os.Bundle;
import android.widget.Button;

import com.example.vota.util.SessionManager;

public class AdminActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!"admin".equals(new SessionManager(this).role())) {
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
    }
}