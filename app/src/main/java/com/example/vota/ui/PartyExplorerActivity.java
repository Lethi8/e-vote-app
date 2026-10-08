package com.example.vota.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

        TextView title = findViewById(R.id.txtTitle);
        TextView subtitle = findViewById(R.id.txtSubtitle);

        title.setText("Party explorer");
        subtitle.setText(
                "Compare party information before making an informed choice. "
                        + "Vota does not endorse any political party."
        );

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        edtSearch = findViewById(R.id.edtSearch);
        spnSort = findViewById(R.id.spnSort);
        txtEmpty = findViewById(R.id.txtEmpty);

        edtSearch.setVisibility(View.VISIBLE);
        spnSort.setVisibility(View.VISIBLE);

        setupSearch();
        setupSort();

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
                            showError(
                                    "Could not load parties. HTTP "
                                            + response.code()
                            );
                            return;
                        }

                        List<Party> loadedParties =
                                response.body();

                        allParties.clear();

                        if (loadedParties != null) {
                            allParties.addAll(loadedParties);
                        }

                        applyFilters();

                        if (!allParties.isEmpty()) {
                            awardPartyExplorerBadge(session);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<Party>> call,
                            Throwable t
                    ) {

                        String message = t.getMessage();

                        if (message == null
                                || message.trim().isEmpty()) {
                            message = "Unknown network error";
                        }

                        showError(
                                "Network error: " + message
                        );
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

    private void setupSearch() {
        edtSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        currentSearch =
                                s == null
                                        ? ""
                                        : s.toString()
                                        .trim()
                                        .toLowerCase(Locale.ROOT);

                        applyFilters();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
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
        );
    }

    private void applyFilters() {
        List<Party> filtered =
                new ArrayList<>();

        for (Party party : allParties) {

            String name =
                    safe(party.name)
                            .toLowerCase(Locale.ROOT);

            String abbreviation =
                    safe(party.abbreviation)
                            .toLowerCase(Locale.ROOT);

            String description =
                    safe(party.description)
                            .toLowerCase(Locale.ROOT);

            boolean matches =
                    currentSearch.isEmpty()
                            || name.contains(currentSearch)
                            || abbreviation.contains(currentSearch)
                            || description.contains(currentSearch);

            if (matches) {
                filtered.add(party);
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
