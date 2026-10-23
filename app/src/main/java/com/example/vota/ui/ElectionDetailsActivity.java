
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

    //fetches specific election by id before rendering anything
    private boolean checkingVote = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        api = ApiClient.get(session)
                .create(RestApi.class);

        String electionId =
                getIntent().getStringExtra("election_id");

        if (electionId == null
                || electionId.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Election ID is missing.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        page(
                "LOADING",
                "Loading election...",
                "Please wait."
        );

        loadElection(electionId);
    }
// builds the content page and shows the "Cast ballot" option only if the election is active

    // =========================================
    // LOAD SELECTED ELECTION
    // =========================================

    private void loadElection(String electionId) {

        api.electionById("eq." + electionId)
                .enqueue(new Callback<List<Election>>() {

                    @Override
                    public void onResponse(
                            Call<List<Election>> call,
                            Response<List<Election>> response) {

                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body().isEmpty()) {

                            Toast.makeText(
                                    ElectionDetailsActivity.this,
                                    "Could not load election.",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                            return;
                        }

                        election = response.body().get(0);

                        if (election.id == null
                                || !election.id.equals(electionId)) {

                            Toast.makeText(
                                    ElectionDetailsActivity.this,
                                    "Election ID mismatch.",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                            return;
                        }

                        render();
                    }

                    @Override
                    public void onFailure(
                            Call<List<Election>> call,
                            Throwable t) {

                        Toast.makeText(
                                ElectionDetailsActivity.this,
                                "Network error. Check your connection.",
                                Toast.LENGTH_LONG
                        ).show();

                        finish();
                    }
                });
    }

    // =========================================
    // DISPLAY ELECTION DETAILS
    // =========================================

    private void render() {

        page(
                election.status,
                election.title,
                election.description
        );

        addSection(
                "Election details",
                election.type + " election"
                        + "\nRegion: " + election.region
                        + "\nOpens: " + election.opensAt
                        + "\nCloses: " + election.closesAt
        );

        addSection(
                "On the ballot",
                "Explore the political parties and candidates "
                        + "participating in this election."
        );

        secondary("Explore parties and candidates")
                .setOnClickListener(v -> {

                    Intent intent = new Intent(
                            this,
                            PartyExplorerActivity.class
                    );

                    intent.putExtra(
                            "election_id",
                            election.id
                    );

                    startActivity(intent);
                });

        if ("ACTIVE".equalsIgnoreCase(election.status)) {

            primary("Continue to ballot or receipt")
                    .setOnClickListener(v ->
                            checkVotingStatus());

        } else if (election.resultsPublished) {

            primary("View published results")
                    .setOnClickListener(v -> {

                        Intent intent = new Intent(
                                this,
                                ResultsActivity.class
                        );

                        intent.putExtra(
                                "election_id",
                                election.id
                        );

                        startActivity(intent);
                    });
        }
    }
//Checks login and verification status then calls myReceipt() to see whether a vote already exists for this election
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

    // =========================================
    // CHECK VOTER STATUS
    // =========================================

    private void checkVotingStatus() {

        if (checkingVote) {
            return;
        }

        if (election == null
                || election.id == null) {
            return;
        }

        if (!session.isLoggedIn()) {
            open(LoginActivity.class);
            return;
        }

        if (!session.isVerified()) {
            open(VerificationActivity.class);
            return;
        }

        checkingVote = true;

        Toast.makeText(
                this,
                "Checking your voting status...",
                Toast.LENGTH_SHORT
        ).show();

        api.myReceipt("eq." + election.id)
                .enqueue(
                        new Callback<List<RestApi.ReceiptRow>>() {

                            @Override
                            public void onResponse(
                                    Call<List<RestApi.ReceiptRow>> call,
                                    Response<List<RestApi.ReceiptRow>> response) {

                                checkingVote = false;

                                if (!response.isSuccessful()) {

                                    Toast.makeText(
                                            ElectionDetailsActivity.this,
                                            "Could not verify voting status. HTTP "
                                                    + response.code(),
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                if (response.body() == null) {

                                    Toast.makeText(
                                            ElectionDetailsActivity.this,
                                            "Voting status unavailable. Try again.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                List<RestApi.ReceiptRow> receipts =
                                        response.body();

                                // Existing participation record found.
                                if (!receipts.isEmpty()) {

                                    RestApi.ReceiptRow receipt =
                                            receipts.get(0);

                                    if (receipt.receipt_hash == null
                                            || receipt.receipt_hash.trim().isEmpty()) {

                                        Toast.makeText(
                                                ElectionDetailsActivity.this,
                                                "An existing vote has no valid receipt.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    openExistingReceipt(
                                            receipt.receipt_hash
                                    );

                                    return;
                                }

                                // No participation record was returned.
                                // The voting backend must still enforce
                                // eligibility and prevent duplicate voting.
                                openVotingScreen();
                            }

                            @Override
                            public void onFailure(
                                    Call<List<RestApi.ReceiptRow>> call,
                                    Throwable t) {

                                checkingVote = false;

                                Toast.makeText(
                                        ElectionDetailsActivity.this,
                                        "Unable to check voting status. Try again.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
    }

    // =========================================
    // OPEN EXISTING RECEIPT
    // =========================================

    private void openExistingReceipt(String receiptHash) {

        Intent intent = new Intent(
                this,
                ReceiptActivity.class
        );

        intent.putExtra(
                "election_id",
                election.id
        );

        intent.putExtra(
                "election_title",
                election.title
        );

        intent.putExtra(
                "receipt",
                receiptHash
        );

        startActivity(intent);
    }

    // =========================================
    // OPEN VOTING SCREEN
    // =========================================

    private void openVotingScreen() {

        Intent intent = new Intent(
                this,
                VoteActivity.class
        );

        intent.putExtra(
                "election_id",
                election.id
        );

        intent.putExtra(
                "election_title",
                election.title
        );

        startActivity(intent);
    }
}
