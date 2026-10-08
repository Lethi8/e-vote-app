package com.example.vota.ui;

import android.os.Bundle;
import android.widget.Toast;

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

        primary("Refresh")
                .setOnClickListener(v -> loadAuditLogs());

        loadAuditLogs();
    }

    private void loadAuditLogs() {

        api.auditLogs().enqueue(new Callback<List<RestApi.AuditLogRow>>() {

            @Override
            public void onResponse(
                    Call<List<RestApi.AuditLogRow>> call,
                    Response<List<RestApi.AuditLogRow>> response
            ) {

                if (!response.isSuccessful() || response.body() == null) {

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

                    return;
                }

                for (RestApi.AuditLogRow log : logs) {

                    String user =
                            log.user_id == null
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
            }

            @Override
            public void onFailure(
                    Call<List<RestApi.AuditLogRow>> call,
                    Throwable t
            ) {

                Toast.makeText(
                        AuditLogsActivity.this,
                        "Network error: "
                                + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private String safe(String value) {
        return value == null
                ? ""
                : value;
    }
}