package com.example.vota.ui;

import android.os.Bundle;

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
        addSection("Platform overview", "Registered voters • votes cast • elections • feedback totals");
        addSection("User management",
                "Review voter status and assign authorised staff roles through a protected server-side operation.");
        addSection("Audit trail",
                "Review privileged actions. Ordinary voters must never read the audit log.");
    }
}