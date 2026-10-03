package com.example.vota.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.vota.model.Election;
import com.example.vota.net.ApiClient;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ElectionDetailsActivity extends BaseActivity {
    private Election election;
    private SessionManager session;
    private RestApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String electionId = getIntent().getStringExtra("election_id");

        session = new SessionManager(this);
        api = ApiClient.get(session).create(RestApi.class);

        page("LOADING", "Loading election…", "");

        api.electionById("eq." + electionId).enqueue(new Callback<List<Election>>() {
            @Override
            public void onResponse(Call<List<Election>> call, Response<List<Election>> resp) {
                if (!resp.isSuccessful() || resp.body() == null || resp.body().isEmpty()) {
                    Toast.makeText(ElectionDetailsActivity.this, "Election not found.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                election = resp.body().get(0);
                render();
            }

            @Override
            public void onFailure(Call<List<Election>> call, Throwable t) {
                Toast.makeText(ElectionDetailsActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void render() {
        page(election.status, election.title, election.description);
        addSection("Election details",
                election.type + " election\nRegion: " + election.region
                        + "\nOpens: " + election.opensAt + "\nCloses: " + election.closesAt);
        addSection("On the ballot", "Approved parties and candidates are available to explore.");

        secondary("Explore parties and candidates").setOnClickListener(v -> {
            Intent i = new Intent(this, PartyExplorerActivity.class);
            i.putExtra("election_id", election.id);
            startActivity(i);
        });

        if ("ACTIVE".equals(election.status)) {
            primary("Cast ballot").setOnClickListener(v -> vote());
        } else if (election.resultsPublished) {
            primary("View published results").setOnClickListener(v -> {
                Intent i = new Intent(this, ResultsActivity.class);
                i.putExtra("election_id", election.id);
                startActivity(i);
            });
        }
    }

    private void vote() {
        if (!session.isLoggedIn()) { open(LoginActivity.class); return; }
        if (!session.isVerified()) { open(VerificationActivity.class); return; }

        api.myReceipt("eq." + election.id).enqueue(new Callback<List<RestApi.ReceiptRow>>() {
            @Override
            public void onResponse(Call<List<RestApi.ReceiptRow>> call, Response<List<RestApi.ReceiptRow>> resp) {
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    Intent i = new Intent(ElectionDetailsActivity.this, ReceiptActivity.class);
                    i.putExtra("election_id", election.id);
                    i.putExtra("election_title", election.title);
                    i.putExtra("receipt", resp.body().get(0).receipt_hash);
                    startActivity(i);
                } else {
                    Intent i = new Intent(ElectionDetailsActivity.this, VoteActivity.class);
                    i.putExtra("election_id", election.id);
                    i.putExtra("election_title", election.title);
                    startActivity(i);
                }
            }

            @Override
            public void onFailure(Call<List<RestApi.ReceiptRow>> call, Throwable t) {
                Toast.makeText(ElectionDetailsActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }
}