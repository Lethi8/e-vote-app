package com.example.vota.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.vota.model.Election;
import com.example.vota.net.ApiClient;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultsActivity extends BaseActivity {

    private static final String TAG = "VOTA_DEBUG";

    private RestApi api;
    private String activeElectionId;

    private final Handler autoRefreshHandler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (activeElectionId != null) {
                loadResults(activeElectionId, false);
            }
            autoRefreshHandler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activeElectionId = getIntent().getStringExtra("election_id");

        Log.d(TAG, "onCreate: Initial activeElectionId from Intent = " + activeElectionId);

        setupHeader();

        SessionManager session = new SessionManager(this);
        api = ApiClient.get(session).create(RestApi.class);

        if (activeElectionId != null) {
            loadResults(activeElectionId, true);
            startLiveUpdates();
        } else {
            Log.d(TAG, "activeElectionId is null, fetching elections list...");
            api.elections().enqueue(new Callback<List<Election>>() {
                @Override
                public void onResponse(Call<List<Election>> call, Response<List<Election>> resp) {
                    Log.d(TAG, "api.elections response code: " + resp.code());
                    if (resp.isSuccessful() && resp.body() != null) {
                        Log.d(TAG, "Elections count fetched: " + resp.body().size());
                        for (Election e : resp.body()) {
                            Log.d(TAG, "Election found: ID=" + e.id + ", resultsPublished=" + e.resultsPublished);
                            if (e.resultsPublished) {
                                activeElectionId = e.id;
                                Log.d(TAG, "Selected active election ID: " + activeElectionId);
                                loadResults(activeElectionId, true);
                                startLiveUpdates();
                                return;
                            }
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch elections or body is null.");
                    }
                    setupHeader();
                    addSection("No results yet", "No election currently has published results.");
                }

                @Override
                public void onFailure(Call<List<Election>> call, Throwable t) {
                    Log.e(TAG, "api.elections network error: " + t.getMessage(), t);
                    Toast.makeText(ResultsActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void setupHeader() {
        page("PUBLIC AGGREGATES", "Live results",
                "Only aggregate totals are published. Individual ballot choices are never displayed or linked to voter profiles.");
    }

    private void loadResults(String electionId, boolean showInitialState) {
        String filter = electionId.startsWith("eq.") ? electionId : "eq." + electionId;
        Log.d(TAG, "loadResults called with filter: " + filter + " (showInitialState=" + showInitialState + ")");

        api.results(filter).enqueue(new Callback<List<RestApi.ResultRow>>() {
            @Override
            public void onResponse(Call<List<RestApi.ResultRow>> call, Response<List<RestApi.ResultRow>> resp) {
                Log.d(TAG, "api.results response HTTP code: " + resp.code());

                List<RestApi.ResultRow> results = resp.body();

                if (!resp.isSuccessful() || results == null || results.isEmpty()) {
                    Log.w(TAG, "Filtered query returned empty/error. Attempting fallback query...");
                    fetchUnfilteredResults(electionId, showInitialState);
                    return;
                }

                Log.d(TAG, "Successfully retrieved " + results.size() + " filtered result rows!");
                renderPartyCardsUI(results);
            }

            @Override
            public void onFailure(Call<List<RestApi.ResultRow>> call, Throwable t) {
                Log.e(TAG, "Network error on filtered results: " + t.getMessage());
                fetchUnfilteredResults(electionId, showInitialState);
            }
        });
    }

    private void fetchUnfilteredResults(String rawElectionId, boolean showInitialState) {
        String targetId = rawElectionId.replace("eq.", "");

        api.results(null).enqueue(new Callback<List<RestApi.ResultRow>>() {
            @Override
            public void onResponse(Call<List<RestApi.ResultRow>> call, Response<List<RestApi.ResultRow>> resp) {
                if (!resp.isSuccessful() || resp.body() == null || resp.body().isEmpty()) {
                    if (showInitialState) {
                        setupHeader();
                        addSection("No results yet", "Results have not been published for this election.");
                    }
                    return;
                }

                List<RestApi.ResultRow> filteredList = new ArrayList<>();
                for (RestApi.ResultRow row : resp.body()) {
                    if (targetId.equalsIgnoreCase(row.election_id)) {
                        filteredList.add(row);
                    }
                }

                if (filteredList.isEmpty()) {
                    Log.w(TAG, "No local matches for ID " + targetId + ". Displaying all fetched candidate rows.");
                    renderPartyCardsUI(resp.body());
                } else {
                    renderPartyCardsUI(filteredList);
                }
            }

            @Override
            public void onFailure(Call<List<RestApi.ResultRow>> call, Throwable t) {
                if (showInitialState) {
                    Toast.makeText(ResultsActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void renderPartyCardsUI(List<RestApi.ResultRow> rawResults) {
        Map<String, Long> partyVotesMap = new HashMap<>();
        long totalVotes = 0;

        for (RestApi.ResultRow r : rawResults) {
            if (r.party_name == null || r.party_name.trim().isEmpty()) continue;
            String name = r.party_name.trim();
            long currentVotes = partyVotesMap.containsKey(name) ? partyVotesMap.get(name) : 0;
            partyVotesMap.put(name, currentVotes + r.vote_count);
            totalVotes += r.vote_count;
        }

        List<Map.Entry<String, Long>> sortedParties = new ArrayList<>(partyVotesMap.entrySet());
        sortedParties.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        setupHeader();

        int rank = 1;
        for (Map.Entry<String, Long> entry : sortedParties) {
            String partyName = entry.getKey();
            long votes = entry.getValue();
            double pct = totalVotes == 0 ? 0 : (votes * 100.0 / totalVotes);

            String titleString = String.format("#%d  %s", rank, partyName);
            String statsString = String.format("%.2f%%   •   %d %s",
                    pct, votes, (votes == 1 ? "vote" : "votes"));

            addSection(titleString, statsString, (int) Math.round(pct));

            rank++;
        }

        addSection("Participation Overview", "Total Ballots Counted: " + totalVotes);
    }

    private void startLiveUpdates() {
        autoRefreshHandler.removeCallbacks(refreshRunnable);
        autoRefreshHandler.postDelayed(refreshRunnable, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activeElectionId != null) {
            startLiveUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        autoRefreshHandler.removeCallbacks(refreshRunnable);
    }
}