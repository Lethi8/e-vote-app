package com.example.vota.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.vota.R;
import com.example.vota.model.Election;
import com.example.vota.model.Party;
import com.example.vota.net.ApiClient;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPartiesActivity extends BaseActivity {

    private SessionManager session;
    private RestApi api;

    private final List<Election> elections = new ArrayList<>();
    private final List<Party> parties = new ArrayList<>();
    private final Map<String, String> electionNames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        if (!"admin".equalsIgnoreCase(session.role())) {
            Toast.makeText(this, "Admin access required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        api = ApiClient.get(session).create(RestApi.class);

        page(
                "ADMIN • PARTIES",
                "Manage political parties",
                "Add, edit and delete political parties linked to elections."
        );

        primary("Add party").setOnClickListener(v -> showAddPartyDialog());
        secondary("Refresh").setOnClickListener(v -> loadData());

        loadData();
    }

    private void loadData() {
        showLoading();

        api.elections().enqueue(new Callback<List<Election>>() {
            @Override
            public void onResponse(
                    Call<List<Election>> call,
                    Response<List<Election>> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    showError("Could not load elections.\n" + getErrorMessage(response));
                    return;
                }

                elections.clear();
                elections.addAll(response.body());

                electionNames.clear();
                for (Election election : elections) {
                    electionNames.put(election.id, election.title);
                }

                loadParties();
            }

            @Override
            public void onFailure(Call<List<Election>> call, Throwable t) {
                showError("Could not connect to Supabase.\n" + t.getMessage());
            }
        });
    }

    private void loadParties() {
        api.partiesAll().enqueue(new Callback<List<Party>>() {
            @Override
            public void onResponse(
                    Call<List<Party>> call,
                    Response<List<Party>> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    showError("Could not load parties.\n" + getErrorMessage(response));
                    return;
                }

                parties.clear();
                parties.addAll(response.body());
                displayParties();
            }

            @Override
            public void onFailure(Call<List<Party>> call, Throwable t) {
                showError("Could not connect to Supabase.\n" + t.getMessage());
            }
        });
    }

    private void displayParties() {
        clearPartyCards();

        if (parties.isEmpty()) {
            addSection("No political parties", "No parties have been added yet.");
            return;
        }

        for (Party party : parties) {
            String electionName =
                    electionNames.containsKey(party.electionId)
                            ? electionNames.get(party.electionId)
                            : "Unknown election";

            String body =
                    "Election: " + electionName
                            + "\n"
                            + "Abbreviation: " + safe(party.abbreviation)
                            + "\n"
                            + "Description: " + safe(party.description)
                            + "\n"
                            + "Policy summary: " + safe(party.policySummary)
                            + "\n"
                            + "Key positions: " + safe(party.keyPositions)
                            + "\n"
                            + "Colour: " + safe(party.color);

            addActionSection(
                    safe(party.name),
                    body,
                    "Manage",
                    v -> showPartyOptions(party)
            );
        }
    }

    private void showLoading() {
        clearPartyCards();
        addSection("Loading", "Fetching political parties from Supabase...");
    }

    private void showError(String message) {
        clearPartyCards();
        addSection("Unable to load parties", message);
    }

    private void clearPartyCards() {
        LinearLayout container = findViewById(R.id.contentContainer);
        if (container != null) {
            container.removeAllViews();
        }
    }

    private void showPartyOptions(Party party) {
        String[] options = {
                "View details",
                "Edit party",
                "Delete party",
                "Cancel"
        };

        new AlertDialog.Builder(this)
                .setTitle(party.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showPartyDetails(party);
                    } else if (which == 1) {
                        showEditPartyDialog(party);
                    } else if (which == 2) {
                        confirmDeleteParty(party);
                    }
                })
                .show();
    }

    private void showPartyDetails(Party party) {
        String electionName =
                electionNames.containsKey(party.electionId)
                        ? electionNames.get(party.electionId)
                        : "Unknown election";

        String details =
                "Election: " + electionName
                        + "\n\n"
                        + "Party name: " + safe(party.name)
                        + "\n"
                        + "Abbreviation: " + safe(party.abbreviation)
                        + "\n\n"
                        + "Description:\n" + safe(party.description)
                        + "\n\n"
                        + "Policy summary:\n" + safe(party.policySummary)
                        + "\n\n"
                        + "Key positions:\n" + safe(party.keyPositions)
                        + "\n\n"
                        + "Colour: " + safe(party.color);

        new AlertDialog.Builder(this)
                .setTitle(safe(party.name))
                .setMessage(details)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showAddPartyDialog() {
        if (elections.isEmpty()) {
            Toast.makeText(
                    this,
                    "Create an election before adding a party.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        PartyForm form = createPartyForm(null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add political party")
                .setView(form.container)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                            if (!validateForm(form)) {
                                return;
                            }

                            Party newParty = partyFromForm(form);
                            createParty(newParty, dialog);
                        })
        );

        dialog.show();
    }

    private void createParty(Party party, AlertDialog dialog) {
        api.createParty(
                "return=minimal",
                party
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(
                            AdminPartiesActivity.this,
                            "Party added successfully",
                            Toast.LENGTH_SHORT
                    ).show();
                    loadData();
                } else {
                    Toast.makeText(
                            AdminPartiesActivity.this,
                            "Add failed: " + getErrorMessage(response),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(
                        AdminPartiesActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showEditPartyDialog(Party existingParty) {
        PartyForm form = createPartyForm(existingParty);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit political party")
                .setView(form.container)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                            if (!validateForm(form)) {
                                return;
                            }

                            Party updatedParty = partyFromForm(form);
                            updateParty(existingParty.id, updatedParty, dialog);
                        })
        );

        dialog.show();
    }

    private void updateParty(
            String partyId,
            Party party,
            AlertDialog dialog
    ) {
        api.updateParty(
                "eq." + partyId,
                "return=minimal",
                party
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(
                            AdminPartiesActivity.this,
                            "Party updated successfully",
                            Toast.LENGTH_SHORT
                    ).show();
                    loadData();
                } else {
                    Toast.makeText(
                            AdminPartiesActivity.this,
                            "Update failed: " + getErrorMessage(response),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(
                        AdminPartiesActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void confirmDeleteParty(Party party) {
        new AlertDialog.Builder(this)
                .setTitle("Delete party?")
                .setMessage("Are you sure you want to delete " + party.name + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteParty(party))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteParty(Party party) {
        api.deleteParty(
                "eq." + party.id
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(
                            AdminPartiesActivity.this,
                            "Party deleted",
                            Toast.LENGTH_SHORT
                    ).show();
                    loadData();
                } else {
                    Toast.makeText(
                            AdminPartiesActivity.this,
                            "Delete failed: " + getErrorMessage(response),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(
                        AdminPartiesActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private PartyForm createPartyForm(Party party) {
        PartyForm form = new PartyForm();

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);

        int padding =
                (int) (20 * getResources().getDisplayMetrics().density);

        container.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        form.container = container;

        form.electionSpinner = new Spinner(this);

        List<String> electionTitles = new ArrayList<>();
        for (Election election : elections) {
            electionTitles.add(election.title);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        electionTitles
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        form.electionSpinner.setAdapter(adapter);
        container.addView(form.electionSpinner);

        form.name = createField("Party name");
        container.addView(form.name);

        form.abbreviation = createField("Abbreviation e.g. ANC");
        container.addView(form.abbreviation);

        form.description = createField("Description");
        container.addView(form.description);

        form.color = createField("Colour e.g. #FF0000");
        container.addView(form.color);

        form.policySummary = createLargeField("Policy summary");
        container.addView(form.policySummary);

        form.keyPositions = createLargeField("Key positions");
        container.addView(form.keyPositions);

        if (party != null) {
            form.name.setText(safe(party.name));
            form.abbreviation.setText(safe(party.abbreviation));
            form.description.setText(safe(party.description));
            form.color.setText(safe(party.color));
            form.policySummary.setText(safe(party.policySummary));
            form.keyPositions.setText(safe(party.keyPositions));

            for (int i = 0; i < elections.size(); i++) {
                if (elections.get(i).id.equals(party.electionId)) {
                    form.electionSpinner.setSelection(i);
                    break;
                }
            }
        }

        return form;
    }

    private EditText createField(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setSingleLine(true);
        return field;
    }

    private EditText createLargeField(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setMinLines(3);
        field.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );
        return field;
    }

    private boolean validateForm(PartyForm form) {
        String name =
                form.name.getText().toString().trim();

        String abbreviation =
                form.abbreviation.getText().toString().trim();

        String color =
                form.color.getText().toString().trim();

        if (name.isEmpty()) {
            form.name.setError("Party name is required");
            form.name.requestFocus();
            return false;
        }

        if (abbreviation.isEmpty()) {
            form.abbreviation.setError("Abbreviation is required");
            form.abbreviation.requestFocus();
            return false;
        }

        if (abbreviation.length() > 10) {
            form.abbreviation.setError(
                    "Abbreviation must be 10 characters or less"
            );
            form.abbreviation.requestFocus();
            return false;
        }

        if (!color.isEmpty()
                && !color.matches("^#[0-9A-Fa-f]{6}$")) {

            form.color.setError("Use a colour like #FF0000");
            form.color.requestFocus();
            return false;
        }

        if (elections.isEmpty()) {
            Toast.makeText(
                    this,
                    "No election available",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        if (form.electionSpinner.getSelectedItemPosition() < 0) {
            Toast.makeText(
                    this,
                    "Please select an election",
                    Toast.LENGTH_SHORT
            ).show();
            return false;
        }

        return true;
    }

    private Party partyFromForm(PartyForm form) {
        Party party = new Party();

        Election selectedElection =
                elections.get(
                        form.electionSpinner.getSelectedItemPosition()
                );

        party.electionId = selectedElection.id;
        party.name = form.name.getText().toString().trim();
        party.abbreviation = form.abbreviation.getText().toString().trim();
        party.description = form.description.getText().toString().trim();
        party.color = form.color.getText().toString().trim();
        party.policySummary = form.policySummary.getText().toString().trim();
        party.keyPositions = form.keyPositions.getText().toString().trim();

        return party;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String getErrorMessage(Response<?> response) {
        if (response.errorBody() == null) {
            return "HTTP " + response.code();
        }

        try {
            return response.errorBody().string();
        } catch (IOException e) {
            return "HTTP " + response.code();
        }
    }

    private static class PartyForm {
        LinearLayout container;
        Spinner electionSpinner;
        EditText name;
        EditText abbreviation;
        EditText description;
        EditText color;
        EditText policySummary;
        EditText keyPositions;
    }
}