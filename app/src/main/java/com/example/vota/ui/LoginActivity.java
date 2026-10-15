
package com.example.vota.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vota.R;
import com.example.vota.net.ApiClient;
import com.example.vota.net.AuthApi;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        email = findViewById(R.id.edtEmail);
        password = findViewById(R.id.edtPassword);

        findViewById(R.id.btnDemoVoter)
                .setVisibility(View.GONE);

        findViewById(R.id.btnSignIn)
                .setOnClickListener(v -> signIn());

        findViewById(R.id.btnRegister)
                .setOnClickListener(v ->
                        open(RegisterActivity.class));
    }

    // LOGIN WITH SUPABASE
    private void signIn() {

        String e = email.getText().toString().trim();
        String p = password.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            email.setError("Enter a valid email address");
            return;
        }

        if (p.length() < 8) {
            password.setError(
                    "Password must have at least 8 characters");
            return;
        }

        SessionManager session = new SessionManager(this);

        AuthApi api = ApiClient.get(session)
                .create(AuthApi.class);

        api.signIn(
                "password",
                new AuthApi.SignInRequest(e, p)
        ).enqueue(new Callback<AuthApi.AuthResponse>() {

            @Override
            public void onResponse(
                    Call<AuthApi.AuthResponse> call,
                    Response<AuthApi.AuthResponse> resp) {

                AuthApi.AuthResponse body = resp.body();

                if (!resp.isSuccessful()
                        || body == null
                        || body.accessToken == null
                        || body.user == null
                        || body.user.id == null) {

                    Toast.makeText(
                            LoginActivity.this,
                            "Sign-in failed. Check your email and password.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                session.saveSession(
                        body.user.id,
                        body.accessToken,
                        body.refreshToken
                );

                session.cacheEmail(e);

                loadProfileThenContinue(session);
            }

            @Override
            public void onFailure(
                    Call<AuthApi.AuthResponse> call,
                    Throwable t) {

                Toast.makeText(
                        LoginActivity.this,
                        "Network error. Check your connection.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // LOAD USER PROFILE
    private void loadProfileThenContinue(
            SessionManager session) {

        RestApi api = ApiClient.get(session)
                .create(RestApi.class);

        api.myProfile(
                "eq." + session.userId()
        ).enqueue(new Callback<List<RestApi.Profile>>() {

            @Override
            public void onResponse(
                    Call<List<RestApi.Profile>> call,
                    Response<List<RestApi.Profile>> resp) {

                if (!resp.isSuccessful()) {
                    session.logout();

                    Toast.makeText(
                            LoginActivity.this,
                            "Profile loading failed. HTTP "
                                    + resp.code(),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                if (resp.body() == null
                        || resp.body().isEmpty()) {
                    session.logout();

                    Toast.makeText(
                            LoginActivity.this,
                            "No profile found for this account.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                RestApi.Profile profile =
                        resp.body().get(0);

                if (profile.role == null
                        || profile.role.trim().isEmpty()) {
                    session.logout();

                    Toast.makeText(
                            LoginActivity.this,
                            "Account role is missing.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                session.cacheProfile(
                        profile.full_name,
                        profile.role.trim(),
                        profile.verified
                );

                // NEW DESTINATION: SEPARATE MAIN MENU
                open(MenuActivity.class);
                finish();
            }

            @Override
            public void onFailure(
                    Call<List<RestApi.Profile>> call,
                    Throwable t) {

                session.logout();

                Toast.makeText(
                        LoginActivity.this,
                        "Could not load your profile. Check your connection.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
