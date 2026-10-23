package com.example.vota.ui;

import android.os.Bundle;
import android.util.Log;
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
    private static final String TAG = "VOTA_LOGIN";

    private EditText email, password;
    private View btnSignIn;
    private boolean isSigningIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.edtEmail);
        password = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        View demo = findViewById(R.id.btnDemoVoter);
        if (demo != null) demo.setVisibility(View.GONE);

        btnSignIn.setOnClickListener(v -> signIn());
        findViewById(R.id.btnRegister).setOnClickListener(v -> open(RegisterActivity.class));
    }

    private void setBusy(boolean busy) {
        isSigningIn = busy;
        btnSignIn.setEnabled(!busy);
    }

    private void toast(String msg) {
        if (!isFinishing() && !isDestroyed()) Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    // Validates locally, then calls AuthApi.signIn(). On success, saves the
    // real session token and fetches the profile before continuing to Home.
    private void signIn() {
        if (isSigningIn) return;

        final String e = email.getText().toString().trim();
        final String p = password.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            email.setError("Enter a valid email address");
            email.requestFocus();
            return;
        }
        if (p.length() < 8) {
            password.setError("Password must have at least 8 characters");
            password.requestFocus();
            return;
        }

        setBusy(true);

        final SessionManager session = new SessionManager(this);
        final AuthApi api;
        try {
            api = ApiClient.get(session).create(AuthApi.class);
        } catch (Exception ex) {
            // Surfaces Retrofit setup problems (bad base URL, bad annotations)
            // instead of crashing.
            Log.e(TAG, "Could not create API client", ex);
            setBusy(false);
            toast("App configuration error. Check the Supabase URL and key.");
            return;
        }

        api.signIn("password", new AuthApi.SignInRequest(e, p)).enqueue(new Callback<AuthApi.AuthResponse>() {
            @Override
            public void onResponse(Call<AuthApi.AuthResponse> call, Response<AuthApi.AuthResponse> resp) {
                setBusy(false);
                AuthApi.AuthResponse body = resp.body();

                if (!resp.isSuccessful() || body == null || body.accessToken == null
                        || body.user == null || body.user.id == null) {
                    logErrorBody(resp);
                    toast("Sign-in failed. Check your email, password, and email verification.");
                    return;
                }

                session.saveSession(body.user.id, body.accessToken, body.refreshToken);
                session.cacheEmail(e);
                loadProfileThenContinue(session);
            }

            @Override
            public void onFailure(Call<AuthApi.AuthResponse> call, Throwable t) {
                setBusy(false);
                Log.e(TAG, "Sign-in request failed", t);
                toast("Network error. Check your connection and try again.");
            }
        });
    }

    // Fetches the signed-in user's profile row and caches it locally, then
    // continues to Home either way — a failed fetch shouldn't block a valid session.
    private void loadProfileThenContinue(final SessionManager session) {
        RestApi api = ApiClient.get(session).create(RestApi.class);
        api.myProfile("eq." + session.userId()).enqueue(new Callback<List<RestApi.Profile>>() {
            @Override
            public void onResponse(Call<List<RestApi.Profile>> call, Response<List<RestApi.Profile>> resp) {
                List<RestApi.Profile> list = resp.body();
                if (resp.isSuccessful() && list != null && !list.isEmpty()) {
                    RestApi.Profile profile = list.get(0);
                    session.cacheProfile(profile.full_name, profile.role, profile.verified);
                } else {
                    logErrorBody(resp);
                }
                goHome();
            }

            @Override
            public void onFailure(Call<List<RestApi.Profile>> call, Throwable t) {
                Log.e(TAG, "Failed to load user profile", t);
                goHome();
            }
        });
    }

    private void goHome() {
        if (isFinishing() || isDestroyed()) return;
        open(HomeActivity.class);
        finish();
    }

    private void logErrorBody(Response<?> resp) {
        try {
            if (resp.errorBody() != null) {
                Log.e(TAG, "HTTP " + resp.code() + ": " + resp.errorBody().string());
            } else {
                Log.e(TAG, "HTTP " + resp.code() + " with empty or invalid body");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Could not read error body", ex);
        }
    }
package com.example.vota.ui;

import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "VOTA_LOGIN";

    private EditText email;
    private EditText password;
    private View btnSignIn;
    private boolean isSigningIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.edtEmail);
        password = findViewById(R.id.edtPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        View demo = findViewById(R.id.btnDemoVoter);
        if (demo != null) {
            demo.setVisibility(View.GONE);
        }

        btnSignIn.setOnClickListener(v -> signIn());

        findViewById(R.id.btnRegister).setOnClickListener(
                v -> open(RegisterActivity.class)
        );
    }

    private void setBusy(boolean busy) {
        isSigningIn = busy;
        btnSignIn.setEnabled(!busy);
    }

    private void toast(String msg) {
        if (!isFinishing() && !isDestroyed()) {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }

    private void signIn() {
        if (isSigningIn) {
            return;
        }

        final String e = email.getText().toString().trim();
        final String p = password.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            email.setError("Enter a valid email address");
            email.requestFocus();
            return;
        }

        if (p.length() < 8) {
            password.setError("Password must have at least 8 characters");
            password.requestFocus();
            return;
        }

        setBusy(true);

        final SessionManager session = new SessionManager(getApplicationContext());
        final AuthApi api;
        try {
            api = ApiClient.get(session).create(AuthApi.class);
        } catch (Exception ex) {
            // Surfaces Retrofit setup problems (bad base URL, bad annotations) instead of crashing.
            Log.e(TAG, "Could not create API client", ex);
            setBusy(false);
            toast("App configuration error. Check the Supabase URL and key.");
            return;
        }

        AuthApi.SignInRequest request = new AuthApi.SignInRequest(e, p);

        api.signIn("password", request).enqueue(new Callback<AuthApi.AuthResponse>() {

            @Override
            public void onResponse(Call<AuthApi.AuthResponse> call,
                                   Response<AuthApi.AuthResponse> resp) {
                setBusy(false);

                AuthApi.AuthResponse body = resp.body();

                if (!resp.isSuccessful()
                        || body == null
                        || body.accessToken == null
                        || body.user == null
                        || body.user.id == null) {

                    logErrorBody(resp);
                    toast("Username or Password incorrect.");
                    return;
                }

                session.saveSession(body.user.id, body.accessToken, body.refreshToken);
                session.cacheEmail(e);

                loadProfileThenContinue(session);
            }

            @Override
            public void onFailure(Call<AuthApi.AuthResponse> call, Throwable t) {
                setBusy(false);
                Log.e(TAG, "Sign-in request failed", t);
                toast("Network error. Check your connection and try again.");
            }
        });
    }

    private void loadProfileThenContinue(final SessionManager session) {
        RestApi api = ApiClient.get(session).create(RestApi.class);

        api.myProfile("eq." + session.userId()).enqueue(new Callback<List<RestApi.Profile>>() {

            @Override
            public void onResponse(Call<List<RestApi.Profile>> call,
                                   Response<List<RestApi.Profile>> resp) {
                List<RestApi.Profile> list = resp.body();

                if (resp.isSuccessful() && list != null && !list.isEmpty()) {
                    RestApi.Profile profile = list.get(0);

                    // Boolean.TRUE.equals handles a null "verified" column safely.
                    session.cacheProfile(
                            profile.full_name,
                            profile.role,
                            Boolean.TRUE.equals(profile.verified)
                    );
                } else {
                    logErrorBody(resp);
                }

                goHome();
            }

            @Override
            public void onFailure(Call<List<RestApi.Profile>> call, Throwable t) {
                Log.e(TAG, "Failed to load user profile", t);
                // The user is already signed in, so continue with default profile values.
                goHome();
            }
        });
    }

    private void goHome() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        open(HomeActivity.class);
        finish();
    }

    private void logErrorBody(Response<?> resp) {
        try {
            if (resp.errorBody() != null) {
                Log.e(TAG, "HTTP " + resp.code() + ": " + resp.errorBody().string());
            } else {
                Log.e(TAG, "HTTP " + resp.code() + " with empty or invalid body");
            }
        } catch (Exception ex) {
            Log.e(TAG, "Could not read error body", ex);
        }
    }
}