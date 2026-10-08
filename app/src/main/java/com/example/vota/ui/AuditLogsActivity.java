package com.example.vota.ui;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.vota.R;
import com.example.vota.net.ApiClient;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuditLogsActivity extends BaseActivity {

    private SessionManager session;
    private RestApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        if (!"admin".equalsIgnoreCase(session.role())) {
            Toast.makeText(
                    this,
                    "Admin access required",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        api = ApiClient
                .get(session)
                .create(RestApi.class);

        page(
                "ADMIN • AUDIT",
                "Audit logs",
                "Review administrator actions recorded by the system."
        );

        // Show a visible scrollbar when the audit list becomes long.
        enablePageScrollbar();

        // Main refresh button.
        primary("Refresh")
                .setOnClickListener(v -> loadAuditLogs());

        loadAuditLogs();
    }

    private void loadAuditLogs() {
        clearAuditSections();

        addSection(
                "Loading",
                "Fetching audit logs from Supabase..."
        );

        addBackSection();

        api.auditLogs().enqueue(new Callback<List<RestApi.AuditLogRow>>() {

            @Override
            public void onResponse(
                    Call<List<RestApi.AuditLogRow>> call,
                    Response<List<RestApi.AuditLogRow>> response
            ) {

                clearAuditSections();

                if (!response.isSuccessful() || response.body() == null) {

                    addSection(
                            "Unable to load audit logs",
                            "Supabase returned HTTP " + response.code()
                    );

                    addBackSection();

                    Toast.makeText(
                            AuditLogsActivity.this,
                            "Could not load audit logs. HTTP "
                                    + response.code(),
                            Toast.LENGTH_LONG
                    ).show();

                    return;
                }

                List<RestApi.AuditLogRow> logs =
                        response.body();

                if (logs.isEmpty()) {

                    addSection(
                            "No audit logs",
                            "No administrator actions have been recorded yet."
                    );

                    addBackSection();
                    return;
                }

                for (RestApi.AuditLogRow log : logs) {

                    String user =
                            log.user_id == null
                                    || log.user_id.trim().isEmpty()
                                    ? "System / SQL Editor"
                                    : log.user_id;

                    String body =
                            "Action: "
                                    + safe(log.action)
                                    + "\n"
                                    + "User: "
                                    + user
                                    + "\n"
                                    + "Description: "
                                    + safe(log.description)
                                    + "\n"
                                    + "Created: "
                                    + safe(log.created_at);

                    addSection(
                            "Audit #" + log.id,
                            body
                    );
                }

                addBackSection();
            }

            @Override
            public void onFailure(
                    Call<List<RestApi.AuditLogRow>> call,
                    Throwable t
            ) {

                clearAuditSections();

                String error =
                        t.getMessage() == null
                                ? "Unknown network error"
                                : t.getMessage();

                addSection(
                        "Network error",
                        error
                );

                addBackSection();

                Toast.makeText(
                        AuditLogsActivity.this,
                        "Network error: " + error,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void clearAuditSections() {
        LinearLayout container =
                findViewById(R.id.contentContainer);

        if (container != null) {
            container.removeAllViews();
        }
    }

    private String safe(String value) {
        return value == null
                ? ""
                : value;
    }
}
