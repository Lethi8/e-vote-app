package com.example.vota.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vota.R;
import com.example.vota.model.CardItem;
import com.example.vota.model.Party;
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

public class PartyExplorerActivity extends BaseActivity {
    private static final String TAG = "VOTA_DEBUG";
    private RecyclerView recycler;
    private SessionManager session;
    private RestApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ((TextView) findViewById(R.id.txtTitle)).setText("Party explorer");
        ((TextView) findViewById(R.id.txtSubtitle)).setText(
                "Compare summaries before making an informed choice. Vota does not endorse a party.");

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        session = new SessionManager(this);
        api = ApiClient.get(session).create(RestApi.class);
        String electionId = getIntent().getStringExtra("election_id");

        loadPartiesForExplorer(electionId);
    }

    private void loadPartiesForExplorer(String electionId) {
        if (electionId != null && !electionId.isEmpty()) {
            String filter = electionId.startsWith("eq.") ? electionId : "eq." + electionId;
            api.partiesForElection(filter).enqueue(new Callback<List<Party>>() {
                @Override
                public void onResponse(Call<List<Party>> call, Response<List<Party>> resp) {
                    if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                        populateRecyclerView(resp.body());
                    } else {
                        Log.w(TAG, "Filtered party explorer results empty. Attempting partiesAll() fallback...");
                        loadAllPartiesFallback();
                    }
                }

                @Override
                public void onFailure(Call<List<Party>> call, Throwable t) {
                    Log.e(TAG, "Failed filtered party load: " + t.getMessage());
                    loadAllPartiesFallback();
                }
            });
        } else {
            loadAllPartiesFallback();
        }
    }

    private void loadAllPartiesFallback() {
        api.partiesAll().enqueue(new Callback<List<Party>>() {
            @Override
            public void onResponse(Call<List<Party>> call, Response<List<Party>> resp) {
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    populateRecyclerView(resp.body());
                } else {
                    Toast.makeText(PartyExplorerActivity.this, "Could not load parties.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Party>> call, Throwable t) {
                Toast.makeText(PartyExplorerActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateRecyclerView(List<Party> partyList) {
        List<CardItem> cards = new ArrayList<>();
        for (Party p : partyList) {
            String partyDisplayName = p.name;
            if (p.abbreviation != null && !p.abbreviation.isEmpty() && p.name != null && !p.name.contains("(" + p.abbreviation + ")")) {
                partyDisplayName = p.name + " (" + p.abbreviation + ")";
            }

            String bodyText = (p.description != null ? p.description : "")
                    + "\n\nPolicy: " + (p.policySummary != null ? p.policySummary : "N/A")
                    + "\nKey positions: " + (p.keyPositions != null ? p.keyPositions : "N/A");

            cards.add(new CardItem(
                    p.abbreviation != null ? p.abbreviation : "PARTY",
                    partyDisplayName,
                    bodyText,
                    "Information only",
                    null
            ));
        }

        recycler.setAdapter(new CardAdapter(cards));

        awardExplorerBadge();
    }

    private void awardExplorerBadge() {
        Map<String, String> badgeBody = new HashMap<>();
        badgeBody.put("p_code", "party_explorer");
        api.awardBadge(badgeBody).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> badgeCall, Response<Void> badgeResp) {}
            @Override
            public void onFailure(Call<Void> badgeCall, Throwable badgeT) {}
        });
    }
}