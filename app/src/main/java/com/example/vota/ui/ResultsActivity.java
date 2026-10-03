package com.example.vota.ui;

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

public class ResultsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String electionId = getIntent().getStringExtra("election_id");

        page("PUBLIC AGGREGATES", "Live results",
                "Only aggregate totals are published. Individual ballot choices are never displayed or linked to voter profiles.");

        SessionManager session = new SessionManager(this);
        RestApi api = ApiClient.get(session).create(RestApi.class);

        if (electionId != null) {
            loadResults(api, electionId);
        } else {
            api.elections().enqueue(new Callback<List<Election>>() {
                @Override
                public void onResponse(Call<List<Election>> call, Response<List<Election>> resp) {
                    if (resp.isSuccessful() && resp.body() != null) {
                        for (Election e : resp.body()) {
                            if (e.resultsPublished) { loadResults(api, e.id); return; }
                        }
                    }
                    addSection("No results yet", "No election currently has published results.");
                }

                @Override
                public void onFailure(Call<List<Election>> call, Throwable t) {
                    Toast.makeText(ResultsActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void loadResults(RestApi api, String electionId) {
        api.results(electionId).enqueue(new Callback<List<RestApi.ResultRow>>() {
            @Override
            public void onResponse(Call<List<RestApi.ResultRow>> call, Response<List<RestApi.ResultRow>> resp) {
                if (!resp.isSuccessful() || resp.body() == null || resp.body().isEmpty()) {
                    addSection("No results yet", "Results have not been published for this election.");
                    return;
                }
                long total = 0;
                for (RestApi.ResultRow r : resp.body()) total += r.vote_count;

                StringBuilder sb = new StringBuilder();
                for (RestApi.ResultRow r : resp.body()) {
                    double pct = total == 0 ? 0 : (r.vote_count * 100.0 / total);
                    sb.append(r.party_name).append(": ").append(String.format("%.2f%%", pct)).append("\n");
                }
                addSection("Results", sb.toString().trim());
                addSection("Participation", "Total ballots counted: " + total);
            }

            @Override
            public void onFailure(Call<List<RestApi.ResultRow>> call, Throwable t) {
                Toast.makeText(ResultsActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }
}