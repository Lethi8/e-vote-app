package com.example.vota.ui;

import android.os.Bundle;
import android.widget.Toast;

import com.example.vota.model.EducationResource;
import com.example.vota.net.ApiClient;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LearnActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page("VOTER EDUCATION", "Know before you vote",
                "Short, plain-language guides on voting in South Africa.");

        SessionManager session = new SessionManager(this);
        RestApi api = ApiClient.get(session).create(RestApi.class);
        api.educationResources().enqueue(new Callback<List<EducationResource>>() {
            @Override
            public void onResponse(Call<List<EducationResource>> call, Response<List<EducationResource>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(LearnActivity.this, "Could not load resources.", Toast.LENGTH_LONG).show();
                    return;
                }
                for (EducationResource r : resp.body()) {
                    addSection(r.title, r.body);
                }

                RestApi badgeApi = ApiClient.get(session).create(RestApi.class);
                Map<String, String> badgeBody = new HashMap<>();
                badgeBody.put("p_code", "informed_voter");
                badgeApi.awardBadge(badgeBody).enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> badgeCall, Response<Void> badgeResp) {}
                    @Override public void onFailure(Call<Void> badgeCall, Throwable badgeT) {}
                    // fire-and-forget: no UI feedback needed here, and failure is
                    // harmless — it just means the badge doesn't show up until
                    // the next successful call
                });
            }

            @Override
            public void onFailure(Call<List<EducationResource>> call, Throwable t) {
                Toast.makeText(LearnActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }
}