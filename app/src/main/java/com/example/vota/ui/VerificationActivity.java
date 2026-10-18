package com.example.vota.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.vota.R;
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

        Button button = primary("Complete demo verification");
        button.setOnClickListener(v -> runVerification(button));
    }
//Calls the verify_demo() and the stand-in via onClick and will show a thin shimmering progress bar under the button
    //and disables it while the request is being sent
    private void runVerification(Button button) {
        button.setEnabled(false);
        button.setText("Verifying…");

        FrameLayout skeletonFrame = findViewById(R.id.skeletonFrame);
        ShimmerView shimmerOverlay = findViewById(R.id.shimmerOverlay);
        if (skeletonFrame != null && shimmerOverlay != null) {
            skeletonFrame.getLayoutParams().height = 6;
            skeletonFrame.setVisibility(View.VISIBLE);
            shimmerOverlay.startShimmer();
        }

        SessionManager session = new SessionManager(this);
        RestApi api = ApiClient.get(session).create(RestApi.class);
        api.verifyDemo(new HashMap<>()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> resp) {
                resetButton(button, skeletonFrame, shimmerOverlay);
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
                resetButton(button, skeletonFrame, shimmerOverlay);
                Toast.makeText(VerificationActivity.this, "Could not verify — check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }
// re-enables the button and hides the in-flight shimmmer whether the verification succeeded succeeded or failed
    private void resetButton(Button button, FrameLayout skeletonFrame, ShimmerView shimmerOverlay) {
        button.setEnabled(true);
        button.setText("Complete demo verification");
        if (skeletonFrame != null && shimmerOverlay != null) {
            shimmerOverlay.stopShimmer();
            skeletonFrame.setVisibility(View.GONE);
        }
    }
}