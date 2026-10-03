package com.example.vota.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vota.R;
import com.example.vota.model.CardItem;
import com.example.vota.model.Election;
import com.example.vota.net.ApiClient;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ElectionsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ((TextView) findViewById(R.id.txtTitle)).setText("Elections");
        ((TextView) findViewById(R.id.txtSubtitle))
                .setText("Open, upcoming and completed South African elections.");

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        SessionManager session = new SessionManager(this);
        RestApi api = ApiClient.get(session).create(RestApi.class);
        api.elections().enqueue(new Callback<List<Election>>() {
            @Override
            public void onResponse(Call<List<Election>> call, Response<List<Election>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(ElectionsActivity.this, "Could not load elections.", Toast.LENGTH_LONG).show();
                    return;
                }
                List<CardItem> cards = new ArrayList<>();
                for (Election e : resp.body()) {
                    cards.add(new CardItem(e.status, e.title,
                            e.type + " • " + e.region + "\n" + e.description,
                            "View election →",
                            () -> {
                                Intent i = new Intent(ElectionsActivity.this, ElectionDetailsActivity.class);
                                i.putExtra("election_id", e.id);
                                startActivity(i);
                            }));
                }
                recycler.setAdapter(new CardAdapter(cards));
            }

            @Override
            public void onFailure(Call<List<Election>> call, Throwable t) {
                Toast.makeText(ElectionsActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }
}