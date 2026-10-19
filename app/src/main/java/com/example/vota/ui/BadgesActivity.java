package com.example.vota.ui;

import android.os.Bundle;
import android.widget.Toast;

import com.example.vota.net.ApiClient;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BadgesActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page("ENGAGEMENT", "Your badges",
                "Badges encourage learning and participation without rewarding a particular political choice.");

        SessionManager session = new SessionManager(this);
        RestApi api = ApiClient.get(session).create(RestApi.class);
        api.myBadges().enqueue(new Callback<List<RestApi.BadgeRow>>() {
            @Override
            public void onResponse(Call<List<RestApi.BadgeRow>> call, Response<List<RestApi.BadgeRow>> resp) {
                Set<String> earned = new HashSet<>();
                if (resp.isSuccessful() && resp.body() != null) {
                    for (RestApi.BadgeRow b : resp.body()) earned.add(b.code);
                }
                render(earned);
            }

            @Override
            public void onFailure(Call<List<RestApi.BadgeRow>> call, Throwable t) {
                Toast.makeText(BadgesActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
                render(new HashSet<>());
            }
        });
    }

    private void render(Set<String> earned) {
        addSection(badge("registered", earned) + "Registered • 10 points", "Complete voter registration.");
        addSection(badge("verified_voter", earned) + "Verified Voter • 10 points", "Complete identity verification.");
        addSection(badge("informed_voter", earned) + "Informed Voter • 15 points", "Read a voter education guide.");
        addSection(badge("party_explorer", earned) + "Party Explorer • 15 points", "Browse neutral candidate and party information.");
        addSection(badge("first_ballot", earned) + "First Ballot • 50 points",
                "Cast a first ballot. The badge records participation, never the selection.");
    }

    private String badge(String code, Set<String> earned) {
        return earned.contains(code) ? "✓ " : "○ ";
    }
}