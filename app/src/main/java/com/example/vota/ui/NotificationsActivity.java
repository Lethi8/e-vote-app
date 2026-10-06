package com.example.vota.ui;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.vota.service.NotificationHelper;

public class NotificationsActivity extends BaseActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;
    private static final String ELECTION_OPEN_MESSAGE =
            "The 2026 Tshwane Municipal Election is open. Review candidates before voting.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        page("UPDATES", "Notifications", "Messages are private to the signed-in user.");

        addSection("Election open", ELECTION_OPEN_MESSAGE);
        addSection("Protect your account", "Vota staff will never ask for your password or ballot selection.");
        addSection("New voter guide", "Read the guide on recognising election misinformation.");

        showElectionOpenNotification();
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
            // Below Android 13, no runtime permission is required at all
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