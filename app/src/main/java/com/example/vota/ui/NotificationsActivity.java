package com.example.vota.ui;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.vota.net.ApiClient;
import com.example.vota.net.RestApi;
import com.example.vota.service.NotificationHelper;
import com.example.vota.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends BaseActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private static final String ELECTION_OPEN_MESSAGE =
            "The 2026 Tshwane Municipal Election is open. Review candidates before voting.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page("UPDATES", "Notifications", "Messages are private to the signed-in user.");

        showContentSkeleton(3);

        SessionManager session = new SessionManager(this);
        RestApi api = ApiClient.get(session).create(RestApi.class);
        api.myNotifications().enqueue(new Callback<List<RestApi.NotificationRow>>() {
            @Override
            public void onResponse(Call<List<RestApi.NotificationRow>> call, Response<List<RestApi.NotificationRow>> resp) {
                hideContentSkeleton();
                if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                    for (RestApi.NotificationRow n : resp.body()) {
                        addSection(n.title, n.body);
                    }
                } else {
                    showDefaultCards();
                }
            }

            @Override
            public void onFailure(Call<List<RestApi.NotificationRow>> call, Throwable t) {
                hideContentSkeleton();
                showDefaultCards();
            }
        });

        showElectionOpenNotification();
    }

    private void showDefaultCards() {
        addSection("Election open", ELECTION_OPEN_MESSAGE);
        addSection("Protect your account", "Vota staff will never ask for your password or ballot selection.");
        addSection("New voter guide", "Read the guide on recognising election misinformation.");
    }

    private void showElectionOpenNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                NotificationHelper.showNotification(this, "Election Open!", ELECTION_OPEN_MESSAGE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            NotificationHelper.showNotification(this, "Election Open!", ELECTION_OPEN_MESSAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            NotificationHelper.showNotification(this, "Election Open!", ELECTION_OPEN_MESSAGE);
        }
    }
}