package com.example.vota.ui;

import android.os.Bundle;
import android.widget.Toast;

import com.example.vota.net.ApiClient;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerificationActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page("IDENTITY CHECK", "Verify your identity",
                "Production flow: securely verify the voter against an authorised identity source. "
                        + "This demo records only a server-side verified flag and never attaches identity details to the ballot.");
        addSection("Before continuing",
                "You must be a South African citizen, at least 18 years old, and eligible for this election.");

        primary("Complete demo verification").setOnClickListener(v -> {
            SessionManager session = new SessionManager(this);
            RestApi api = ApiClient.get(session).create(RestApi.class);
            api.verifyDemo(new HashMap<>()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> resp) {
                    if (resp.isSuccessful()) {
                        session.cacheProfile(session.name(), session.role(), true);
                        Toast.makeText(VerificationActivity.this, "Identity verified", Toast.LENGTH_SHORT).show();
                        open(HomeActivity.class);
                        finish();
                    } else {
                        Toast.makeText(VerificationActivity.this, "Verification failed. Try again.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(VerificationActivity.this, "Could not verify — check your connection.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}