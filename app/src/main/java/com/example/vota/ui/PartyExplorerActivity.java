package com.example.vota.ui;

import android.os.Bundle;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ((TextView) findViewById(R.id.txtTitle)).setText("Party explorer");
        ((TextView) findViewById(R.id.txtSubtitle)).setText(
                "Compare summaries before making an informed choice. Vota does not endorse a party.");

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        SessionManager session = new SessionManager(this);
        RestApi api = ApiClient.get(session).create(RestApi.class);
        String electionId = getIntent().getStringExtra("election_id");

        Callback<List<Party>> callback = new Callback<List<Party>>() {
            @Override
            public void onResponse(Call<List<Party>> call, Response<List<Party>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(PartyExplorerActivity.this, "Could not load parties.", Toast.LENGTH_LONG).show();
                    return;
                }
                List<CardItem> cards = new ArrayList<>();
                for (Party p : resp.body()) {
                    cards.add(new CardItem(p.abbreviation, p.name,
                            p.description + "\n\nPolicy: " + p.policySummary
                                    + "\nKey positions: " + p.keyPositions,
                            "Information only", null));
                }
                recycler.setAdapter(new CardAdapter(cards));

                RestApi badgeApi = ApiClient.get(session).create(RestApi.class);
                Map<String, String> badgeBody = new HashMap<>();
                badgeBody.put("p_code", "party_explorer");
                badgeApi.awardBadge(badgeBody).enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> badgeCall, Response<Void> badgeResp) {}
                    @Override public void onFailure(Call<Void> badgeCall, Throwable badgeT) {}
                    // fire-and-forget: no UI feedback needed here, and failure is
                    // harmless — it just means the badge doesn't show up until
                    // the next successful call
                });
            }

            @Override
            public void onFailure(Call<List<Party>> call, Throwable t) {
                Toast.makeText(PartyExplorerActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
            }
        };

        if (electionId != null) {
            api.partiesForElection("eq." + electionId).enqueue(callback);
        } else {
            api.partiesAll().enqueue(callback);
        }
    }
}