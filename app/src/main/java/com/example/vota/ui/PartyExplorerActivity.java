package com.example.vota.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PartyExplorerActivity extends BaseActivity {
    private static final String TAG = "VOTA_DEBUG";
    private RecyclerView recycler;
    private SessionManager session;
    private RestApi api;

    private RecyclerView recycler;
    private EditText edtSearch;
    private Spinner spnSort;
    private TextView txtEmpty;

    private final List<Party> allParties = new ArrayList<>();

    private String currentSearch = "";
    private boolean sortAscending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        session = new SessionManager(this);
        api = ApiClient.get(session).create(RestApi.class);
        String electionId = getIntent().getStringExtra("election_id");
        TextView title = findViewById(R.id.txtTitle);
        TextView subtitle = findViewById(R.id.txtSubtitle);

        title.setText("Party explorer");
        subtitle.setText(
                "Compare party information before making an informed choice. "
                        + "Vota does not endorse any political party."
        );

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        FrameLayout skeletonFrame = findViewById(R.id.skeletonFrame);
        LinearLayout skeletonContainer = findViewById(R.id.skeletonContainer);
        ShimmerView shimmerOverlay = findViewById(R.id.shimmerOverlay);
        SkeletonUtil.showSkeletonList(skeletonFrame, skeletonContainer, shimmerOverlay, R.layout.item_skeleton_card, 3);
        recycler.setVisibility(View.GONE);

        SessionManager session = new SessionManager(this);

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
        );
    }

    private void setupSort() {
        List<String> sortOptions = new ArrayList<>();
        sortOptions.add("A-Z");
        sortOptions.add("Z-A");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        sortOptions
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spnSort.setAdapter(adapter);

        spnSort.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        sortAscending = position == 0;
                        applyFilters();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                    }
                }
                recycler.setAdapter(new CardAdapter(cards));

                RestApi badgeApi = ApiClient.get(session).create(RestApi.class);
                Map<String, String> badgeBody = new HashMap<>();
                badgeBody.put("p_code", "party_explorer");
                badgeApi.awardBadge(badgeBody).enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> badgeCall, Response<Void> badgeResp) {}
                    @Override public void onFailure(Call<Void> badgeCall, Throwable badgeT) {}
                });
            }
        }

        Comparator<Party> comparator =
                Comparator.comparing(
                        party -> safe(party.name),
                        String.CASE_INSENSITIVE_ORDER
                );

        if (!sortAscending) {
            comparator = comparator.reversed();
        }

        filtered.sort(comparator);

        displayParties(filtered);
    }

    private void displayParties(
            List<Party> parties
    ) {

        List<CardItem> cards =
                new ArrayList<>();

        for (Party party : parties) {

            String abbreviation =
                    safe(party.abbreviation);

            String name =
                    safeOrFallback(
                            party.name,
                            "Unnamed party"
                    );

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
                            "View details",
                            () -> showPartyDetails(party)
                    )
            );
        }

        recycler.setAdapter(
                new CardAdapter(cards)
        );

        boolean empty =
                cards.isEmpty();

        txtEmpty.setVisibility(
                empty
                        ? View.VISIBLE
                        : View.GONE
        );

        recycler.setVisibility(
                empty
                        ? View.GONE
                        : View.VISIBLE
        );

        if (empty) {
            if (allParties.isEmpty()) {
                txtEmpty.setText(
                        "No political parties are available."
                );
            } else {
                txtEmpty.setText(
                        "No parties match your search."
                );
            }
        }
    }

    private void showPartyDetails(
            Party party
    ) {

        String details =
                "Party name: "
                        + safeOrFallback(
                        party.name,
                        "Not provided"
                )
                        + "\n\n"
                        + "Abbreviation: "
                        + safeOrFallback(
                        party.abbreviation,
                        "Not provided"
                )
                        + "\n\n"
                        + "Description:\n"
                        + safeOrFallback(
                        party.description,
                        "No description provided."
                )
                        + "\n\n"
                        + "Policy summary:\n"
                        + safeOrFallback(
                        party.policySummary,
                        "No policy summary provided."
                )
                        + "\n\n"
                        + "Key positions:\n"
                        + safeOrFallback(
                        party.keyPositions,
                        "No key positions provided."
                )
                        + "\n\n"
                        + "Colour: "
                        + safeOrFallback(
                        party.color,
                        "Not provided"
                );

        new AlertDialog.Builder(this)
                .setTitle(
                        safeOrFallback(
                                party.name,
                                "Party details"
                        )
                )
                .setMessage(details)
                .setPositiveButton(
                        "Close",
                        null
                )
                .show();
    }

    private void showError(
            String message
    ) {

        recycler.setAdapter(
                new CardAdapter(
                        new ArrayList<>()
                )
        );

        recycler.setVisibility(
                View.GONE
        );

        txtEmpty.setText(message);
        txtEmpty.setVisibility(
                View.VISIBLE
        );

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
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

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {
                // Badge failure should not interrupt party browsing.
            }
        });
    }

    private String safe(
            String value
    ) {
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
