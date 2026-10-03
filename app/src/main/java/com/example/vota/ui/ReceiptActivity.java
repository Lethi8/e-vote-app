package com.example.vota.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ReceiptActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String electionId = getIntent().getStringExtra("election_id");
        String title = getIntent().getStringExtra("election_title");
        String receipt = getIntent().getStringExtra("receipt");

        page("BALLOT RECORDED", "Your ballot is recorded",
                "Election: " + (title != null ? title : "")
                        + "\n\nReceipt code:\n" + (receipt != null ? receipt : "")
                        + "\n\nThis code confirms that a ballot was recorded. "
                        + "It does not contain or reveal your selection.");

        primary("Copy receipt code").setOnClickListener(v -> {
            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                    .setPrimaryClip(ClipData.newPlainText("Vota receipt", receipt));
            Toast.makeText(this, "Receipt copied", Toast.LENGTH_SHORT).show();
        });

        secondary("View results").setOnClickListener(v -> {
            Intent i = new Intent(this, ResultsActivity.class);
            i.putExtra("election_id", electionId);
            startActivity(i);
        });
    }
}