package com.example.vota.ui;

import android.os.Bundle;

import com.example.vota.util.SessionManager;

public class OfficerActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String role = new SessionManager(this).role();
        if (!("official".equals(role) || "admin".equals(role))) {
            finish();
            return;
        }
        page("ROLE: OFFICIAL", "Election officer console",
                "Create and update elections, parties, approved candidates and voter education resources through protected backend operations.");
        addSection("Election management",
                "Draft, schedule, open and close an election. Only an administrator may delete one.");
        addSection("Candidate approval",
                "Add candidates and control whether they are visible on public ballots.");
    }
}