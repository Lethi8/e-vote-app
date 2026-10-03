package com.example.vota.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.vota.R;
import com.example.vota.model.Party;
import com.example.vota.net.ApiClient;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoteActivity extends BaseActivity {
    private String electionId, electionTitle;
    private RadioGroup group;
    private SessionManager session;
    private RestApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        electionId = getIntent().getStringExtra("election_id");
        electionTitle = getIntent().getStringExtra("election_title");

        session = new SessionManager(this);
        if (!session.isLoggedIn() || !session.isVerified()) {
            finish();
            return;
        }
        api = ApiClient.get(session).create(RestApi.class);

        setContentView(R.layout.activity_vote);
        ((TextView) findViewById(R.id.txtElection)).setText(electionTitle != null ? electionTitle : "Your ballot");

        group = findViewById(R.id.radioParties);
        findViewById(R.id.btnCast).setOnClickListener(v -> review());

        api.partiesForElection("eq." + electionId).enqueue(new Callback<List<Party>>() {
            @Override
            public void onResponse(Call<List<Party>> call, Response<List<Party>> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(VoteActivity.this, "Could not load parties.", Toast.LENGTH_LONG).show();
                    return;
                }
                for (Party p : resp.body()) {
                    RadioButton rb = new RadioButton(VoteActivity.this);
                    rb.setId(View.generateViewId());
                    rb.setTag(p);
                    rb.setText(p.name + " (" + p.abbreviation + ")");
                    rb.setPadding(8, 18, 8, 18);
                    rb.setTextColor(androidx.core.content.ContextCompat.getColor(VoteActivity.this, R.color.vota_text));
                    rb.setTextSize(16);
                    group.addView(rb);
                }
            }

            @Override
            public void onFailure(Call<List<Party>> call, Throwable t) {
                Toast.makeText(VoteActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void review() {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) {
            new AlertDialog.Builder(this).setMessage("Select one party before continuing.").setPositiveButton("OK", null).show();
            return;
        }
        if (!((CheckBox) findViewById(R.id.chkConfirm)).isChecked()) {
            new AlertDialog.Builder(this).setMessage("Confirm that you reviewed the ballot.").setPositiveButton("OK", null).show();
            return;
        }

        Party p = (Party) findViewById(id).getTag();
        new AlertDialog.Builder(this)
                .setTitle("Review your selection")
                .setMessage(p.name + " (" + p.abbreviation + ")\n\n"
                        + "After casting, your selection cannot be changed. The receipt will not reveal this choice.")
                .setNegativeButton("Go back", null)
                .setPositiveButton("Cast ballot", (d, w) -> cast(p))
                .show();
    }

    private void cast(Party p) {
        Map<String, String> body = new HashMap<>();
        body.put("p_election_id", electionId);
        body.put("p_party_id", p.id);

        api.castBallot(body).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    new AlertDialog.Builder(VoteActivity.this)
                            .setMessage(errorMessage(resp))
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }
                Intent i = new Intent(VoteActivity.this, ReceiptActivity.class);
                i.putExtra("election_id", electionId);
                i.putExtra("election_title", electionTitle);
                i.putExtra("receipt", resp.body());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(VoteActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String errorMessage(Response<?> resp) {
        try {
            if (resp.errorBody() != null) {
                String raw = resp.errorBody().string();
                return new JSONObject(raw).optString("message", raw);
            }
        } catch (Exception ignored) {}
        return "Could not cast ballot.";
    }
}