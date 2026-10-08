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

        TextView title = findViewById(R.id.txtTitle);
        TextView subtitle = findViewById(R.id.txtSubtitle);

        title.setText("Party explorer");
        subtitle.setText(
                "Compare party information before making an informed choice. "
                        + "Vota does not endorse any political party."
        );

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        SessionManager session = new SessionManager(this);

        RestApi api = ApiClient
                .get(session)
                .create(RestApi.class);

        String electionId =
                getIntent().getStringExtra("election_id");

        Callback<List<Party>> callback =
                new Callback<List<Party>>() {

                    @Override
                    public void onResponse(
                            Call<List<Party>> call,
                            Response<List<Party>> response
                    ) {

                        if (!response.isSuccessful()) {

                            Toast.makeText(
                                    PartyExplorerActivity.this,
                                    "Could not load parties. "
                                            + "Server returned HTTP "
                                            + response.code()
                                            + ".",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        List<Party> loadedParties =
                                response.body();

                        if (loadedParties == null
                                || loadedParties.isEmpty()) {

                            recycler.setAdapter(
                                    new CardAdapter(
                                            new ArrayList<>()
                                    )
                            );

                            Toast.makeText(
                                    PartyExplorerActivity.this,
                                    "No political parties are available for this election.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        List<CardItem> cards =
                                new ArrayList<>();

                        for (Party party : loadedParties) {

                            String abbreviation =
                                    safe(party.abbreviation);

                            String name =
                                    safe(party.name);

                            String description =
                                    safeOrFallback(
                                            party.description,
                                            "No description provided."
                                    );

                            String policySummary =
                                    safeOrFallback(
                                            party.policySummary,
                                            "No policy summary provided."
                                    );

                            String keyPositions =
                                    safeOrFallback(
                                            party.keyPositions,
                                            "No key positions provided."
                                    );

                            String badge =
                                    abbreviation.isEmpty()
                                            ? "PARTY"
                                            : abbreviation;

                            String body =
                                    description
                                            + "\n\nPolicy summary:\n"
                                            + policySummary
                                            + "\n\nKey positions:\n"
                                            + keyPositions;

                            cards.add(
                                    new CardItem(
                                            badge,
                                            name,
                                            body,
                                            "Information only",
                                            null
                                    )
                            );
                        }

                        recycler.setAdapter(
                                new CardAdapter(cards)
                        );

                        awardPartyExplorerBadge(session);
                    }

                    @Override
                    public void onFailure(
                            Call<List<Party>> call,
                            Throwable t
                    ) {

                        String message =
                                t.getMessage();

                        if (message == null
                                || message.trim().isEmpty()) {

                            message =
                                    "Unknown network error";
                        }

                        Toast.makeText(
                                PartyExplorerActivity.this,
                                "Network error: "
                                        + message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                };

        if (electionId != null
                && !electionId.trim().isEmpty()) {

            api.partiesForElection(
                    "eq." + electionId
            ).enqueue(callback);

        } else {

            api.partiesAll()
                    .enqueue(callback);
        }
    }

    private void awardPartyExplorerBadge(
            SessionManager session
    ) {

        RestApi badgeApi =
                ApiClient
                        .get(session)
                        .create(RestApi.class);

        Map<String, String> badgeBody =
                new HashMap<>();

        badgeBody.put(
                "p_code",
                "party_explorer"
        );

        badgeApi.awardBadge(
                badgeBody
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {
                // No UI feedback needed.
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {
                // Badge failure should not interrupt party browsing.
            }
        });
    }

    private String safe(String value) {
        return value == null
                ? ""
                : value.trim();
    }

    private String safeOrFallback(
            String value,
            String fallback
    ) {

        String cleaned =
                safe(value);

        return cleaned.isEmpty()
                ? fallback
                : cleaned;
    }
}
