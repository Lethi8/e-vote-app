package com.example.vota.ui;


import android.os.Bundle;

public class SecurityActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page("PRIVACY AND SECURITY", "How your vote stays secret",
                "The app UI is only one layer. Supabase Row Level Security and protected server functions must enforce every rule.");
        addSection("One person, one ballot",
                "A unique voter-and-election constraint prevents a second ballot for the same election.");
        addSection("Identity separated from choice",
                "Store voter participation independently from anonymous ballot content. The supplied web database still contains voter_id in votes, so strengthen that schema before claiming ballot anonymity.");
        addSection("Receipts you can check",
                "A random receipt proves recording without encoding party or candidate selection.");
        addSection("POPIA-aligned handling",
                "Collect only necessary identity data, restrict access, log privileged actions, define retention periods and support correction/deletion rights where legally applicable.");
    }
}