package com.example.vota.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.vota.R;
import com.example.vota.net.ApiClient;
import com.example.vota.net.AuthApi;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;
import com.example.vota.util.SkeletonUtil;
import com.example.vota.util.ThemeManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends BaseActivity {
    private SessionManager session;
    private RestApi api;
    private AuthApi authApi;
    private RestApi.Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        session = new SessionManager(this);
        api = ApiClient.get(session).create(RestApi.class);
        authApi = ApiClient.get(session).create(AuthApi.class);

        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());
        findViewById(R.id.btnSignOut).setOnClickListener(v -> signOut());
        findViewById(R.id.btnSignOutEverywhere).setOnClickListener(v -> signOutEverywhere());
        findViewById(R.id.btnSecurityInfo).setOnClickListener(v -> open(SecurityActivity.class));
        findViewById(R.id.btnBadges).setOnClickListener(v -> open(BadgesActivity.class));
        findViewById(R.id.btnEditPhone).setOnClickListener(v -> showEditPhoneDialog());
        findViewById(R.id.btnDeleteAccount).setOnClickListener(v -> showDeleteAccountDialog());

        setUpThemeControls();

        FrameLayout accountFrame = findViewById(R.id.accountSkeletonFrame);
        LinearLayout accountSkeletonContainer = findViewById(R.id.accountSkeletonContainer);
        ShimmerView accountShimmer = findViewById(R.id.accountShimmerOverlay);
        SkeletonUtil.showSkeletonList(accountFrame, accountSkeletonContainer, accountShimmer, R.layout.item_skeleton_lines, 1);

        FrameLayout participationFrame = findViewById(R.id.participationSkeletonFrame);
        LinearLayout participationSkeletonContainer = findViewById(R.id.participationSkeletonContainer);
        ShimmerView participationShimmer = findViewById(R.id.participationShimmerOverlay);
        SkeletonUtil.showSkeletonList(participationFrame, participationSkeletonContainer, participationShimmer, R.layout.item_skeleton_lines, 1);

        loadProfile();
        loadParticipationHistory();
    }
//Wires the theme radioGroup to themeManager
    private void setUpThemeControls() {
        RadioGroup radioTheme = findViewById(R.id.radioTheme);
        int currentMode = ThemeManager.getMode(this);
        if (currentMode == ThemeManager.MODE_LIGHT) radioTheme.check(R.id.radioThemeLight);
        else if (currentMode == ThemeManager.MODE_DARK) radioTheme.check(R.id.radioThemeDark);
        else radioTheme.check(R.id.radioThemeSystem);

        radioTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.radioThemeLight) mode = ThemeManager.MODE_LIGHT;
            else if (checkedId == R.id.radioThemeDark) mode = ThemeManager.MODE_DARK;
            else mode = ThemeManager.MODE_SYSTEM;
            ThemeManager.setMode(this, mode);
            recreate();
        });
    }
//Fetches the signed-in users own profile row and renders it
    private void loadProfile() {
        api.myProfile("eq." + session.userId()).enqueue(new Callback<List<RestApi.Profile>>() {
            @Override
            public void onResponse(Call<List<RestApi.Profile>> call, Response<List<RestApi.Profile>> resp) {
                FrameLayout accountFrame = findViewById(R.id.accountSkeletonFrame);
                LinearLayout accountContent = findViewById(R.id.accountContent);
                ShimmerView accountShimmer = findViewById(R.id.accountShimmerOverlay);
                accountShimmer.stopShimmer();
                accountFrame.setVisibility(View.GONE);
                accountContent.setVisibility(View.VISIBLE);

                if (!resp.isSuccessful() || resp.body() == null || resp.body().isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Could not load profile.", Toast.LENGTH_LONG).show();
                    return;
                }
                profile = resp.body().get(0);
                session.cacheProfile(profile.full_name, profile.role, profile.verified);
                render();
            }

            @Override
            public void onFailure(Call<List<RestApi.Profile>> call, Throwable t) {
                FrameLayout accountFrame = findViewById(R.id.accountSkeletonFrame);
                LinearLayout accountContent = findViewById(R.id.accountContent);
                ShimmerView accountShimmer = findViewById(R.id.accountShimmerOverlay);
                accountShimmer.stopShimmer();
                accountFrame.setVisibility(View.GONE);
                accountContent.setVisibility(View.VISIBLE);
                Toast.makeText(ProfileActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }
// Populates every text field on the account card from the fetched profile, hides all but the last 4 digits of the ID number
    private void render() {
        ((TextView) findViewById(R.id.txtName)).setText("Name: " + profile.full_name);
        ((TextView) findViewById(R.id.txtEmail)).setText("Email: " + session.email());
        ((TextView) findViewById(R.id.txtPhone)).setText("Phone: " + (profile.phone != null ? profile.phone : "Not set"));

        String idDisplay = "ID number: ";
        if (profile.sa_id_number != null && profile.sa_id_number.length() == 13) {
            idDisplay += "•••••••••" + profile.sa_id_number.substring(9);
        } else {
            idDisplay += "Not available";
        }
        ((TextView) findViewById(R.id.txtIdMasked)).setText(idDisplay);

        ((TextView) findViewById(R.id.txtVerified)).setText(profile.verified ? "Verification: Verified ✓" : "Verification: Pending");
        ((TextView) findViewById(R.id.txtRole)).setText("Role: " + profile.role);
        ((TextView) findViewById(R.id.txtMemberSince)).setText("Member since: " + (profile.created_at != null && profile.created_at.length() >= 10 ? profile.created_at.substring(0, 10) : ""));

        Button admin = findViewById(R.id.btnAdmin);
        Button officer = findViewById(R.id.btnOfficer);
        if ("admin".equals(profile.role)) {
            admin.setVisibility(View.VISIBLE);
            admin.setOnClickListener(v -> open(AdminActivity.class));
        }
        if ("official".equals(profile.role) || "admin".equals(profile.role)) {
            officer.setVisibility(View.VISIBLE);
            officer.setOnClickListener(v -> open(OfficerActivity.class));
        }
    }
    //Fetches the users election participation history and renders it as a list
    private void loadParticipationHistory() {
        api.myParticipationHistory().enqueue(new Callback<List<RestApi.ParticipationRow>>() {
            @Override
            public void onResponse(Call<List<RestApi.ParticipationRow>> call, Response<List<RestApi.ParticipationRow>> resp) {
                FrameLayout participationFrame = findViewById(R.id.participationSkeletonFrame);
                ShimmerView participationShimmer = findViewById(R.id.participationShimmerOverlay);
                participationShimmer.stopShimmer();
                participationFrame.setVisibility(View.GONE);

                LinearLayout container = findViewById(R.id.participationContainer);
                if (!resp.isSuccessful() || resp.body() == null || resp.body().isEmpty()) {
                    TextView empty = new TextView(ProfileActivity.this);
                    empty.setText("You haven't participated in any elections yet.");
                    container.addView(empty);
                    return;
                }
                for (RestApi.ParticipationRow p : resp.body()) {
                    TextView row = new TextView(ProfileActivity.this);
                    String title = p.elections != null ? p.elections.title : "Election";
                    row.setText("• " + title + " — receipt " + p.receipt_hash);
                    row.setPadding(0, 6, 0, 6);
                    container.addView(row);
                }
            }

            @Override
            public void onFailure(Call<List<RestApi.ParticipationRow>> call, Throwable t) {
                FrameLayout participationFrame = findViewById(R.id.participationSkeletonFrame);
                ShimmerView participationShimmer = findViewById(R.id.participationShimmerOverlay);
                participationShimmer.stopShimmer();
                participationFrame.setVisibility(View.GONE);
            }
        });
    }
//shows a dialog to edit the phone number
    private void showEditPhoneDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setText(profile != null && profile.phone != null ? profile.phone : "");
        new AlertDialog.Builder(this)
                .setTitle("Update phone number")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, w) -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("p_phone", input.getText().toString().trim());
                    api.updateMyPhone(body).enqueue(new Callback<Void>() {
                        @Override public void onResponse(Call<Void> phoneCall, Response<Void> phoneResp) {
                            if (phoneResp.isSuccessful()) { loadProfile(); }
                            else { Toast.makeText(ProfileActivity.this, "Could not update phone.", Toast.LENGTH_LONG).show(); }
                        }
                        @Override public void onFailure(Call<Void> phoneCall, Throwable phoneT) {
                            Toast.makeText(ProfileActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .show();
    }
//shows the dialog for a new password
    private void showChangePasswordDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("New password (at least 8 characters)");
        new AlertDialog.Builder(this)
                .setTitle("Change password")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Update", (d, w) -> {
                    String newPassword = input.getText().toString();
                    //minimum of 8 characters
                    if (newPassword.length() < 8) {
                        Toast.makeText(this, "Use at least 8 characters.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Map<String, String> body = new HashMap<>();
                    body.put("password", newPassword);
                    authApi.updateUser(body).enqueue(new Callback<AuthApi.AuthResponse>() {
                        @Override public void onResponse(Call<AuthApi.AuthResponse> pwCall, Response<AuthApi.AuthResponse> pwResp) {
                            if (pwResp.isSuccessful()) { Toast.makeText(ProfileActivity.this, "Password updated", Toast.LENGTH_SHORT).show(); }
                            else { Toast.makeText(ProfileActivity.this, "Could not update password.", Toast.LENGTH_LONG).show(); }
                        }
                        @Override public void onFailure(Call<AuthApi.AuthResponse> pwCall, Throwable pwT) {
                            Toast.makeText(ProfileActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .show();
    }
// clears local session and goes back to MainActivity
    private void signOut() {
        session.logout();
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
//Revokes every active session for the account
    private void signOutEverywhere() {
        authApi.logout("global").enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> outCall, Response<Void> outResp) { signOut(); }
            @Override public void onFailure(Call<Void> outCall, Throwable outT) {
                Toast.makeText(ProfileActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
            }
        });
    }
//requires the user to type "delete" exactly before proceeding
    private void showDeleteAccountDialog() {
        EditText input = new EditText(this);
        input.setHint("Type DELETE to confirm");
        new AlertDialog.Builder(this)
                .setTitle("Delete your account")
                .setMessage("This permanently deletes your account, profile, and participation records. Votes you've already cast remain counted in results but can never be linked back to you. This cannot be undone.")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (d, w) -> {
                    if (!"DELETE".equals(input.getText().toString().trim())) {
                        Toast.makeText(this, "Type DELETE exactly to confirm.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    api.deleteOwnAccount(new HashMap<>()).enqueue(new Callback<Void>() {
                        @Override public void onResponse(Call<Void> delCall, Response<Void> delResp) {
                            if (delResp.isSuccessful()) {
                                session.logout();
                                startActivity(new Intent(ProfileActivity.this, MainActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                finish();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Could not delete account.", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override public void onFailure(Call<Void> delCall, Throwable delT) {
                            Toast.makeText(ProfileActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .show();
    }
}