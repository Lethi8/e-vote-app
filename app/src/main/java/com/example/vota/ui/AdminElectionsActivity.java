package com.example.vota.ui;

import android.os.Bundle;

import com.example.vota.util.SessionManager;

public class AdminElectionsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);

        if (!"admin".equalsIgnoreCase(sessionManager.role())) {
            finish();
            return;
        }

        page(
                "ADMIN • ELECTIONS",
                "Manage elections",
                "Create and manage elections stored in Supabase."
        );

        addSection(
                "Election management",
                "Your elections will appear here."
        );

        enablePageScrollbar();

        addRefreshSection();

        addBackSection();
    }
}