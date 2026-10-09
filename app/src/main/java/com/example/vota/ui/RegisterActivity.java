
package com.example.vota.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vota.R;
import com.example.vota.net.ApiClient;
import com.example.vota.net.AuthApi;
import com.example.vota.util.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViewById(R.id.btnCreate).setOnClickListener(v -> create());
    }

    private void create() {
        EditText name = findViewById(R.id.edtName);
        EditText email = findViewById(R.id.edtEmail);
        EditText phone = findViewById(R.id.edtPhone);
        EditText id = findViewById(R.id.edtId);
        EditText password = findViewById(R.id.edtPassword);
        EditText confirm = findViewById(R.id.edtConfirm);
        CheckBox consent = findViewById(R.id.chkConsent);

        String fullName = name.getText().toString().trim();
        String emailAddress = email.getText().toString().trim();
        String phoneNumber = phone.getText().toString().trim();
        String idNumber = id.getText().toString().trim();
        String passwordValue = password.getText().toString();

        if (fullName.length() < 3) {
            name.setError("Enter your full name");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            email.setError("Enter a valid email");
            return;
        }

        if (!idNumber.matches("\\d{13}")) {
            id.setError("Enter a 13-digit South African ID number");
            return;
        }

        if (passwordValue.length() < 8) {
            password.setError("Use at least 8 characters");
            return;
        }

        if (!passwordValue.equals(confirm.getText().toString())) {
            confirm.setError("Passwords do not match");
            return;
        }

        if (!consent.isChecked()) {
            consent.setError("Consent is required for verification");
            return;
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("full_name", fullName);
        metadata.put("phone", phoneNumber);
        metadata.put("sa_id_number", idNumber);

        SessionManager session = new SessionManager(this);
        AuthApi api = ApiClient.get(session).create(AuthApi.class);

        AuthApi.SignUpRequest request =
                new AuthApi.SignUpRequest(emailAddress, passwordValue, metadata);

        api.signUp(request).enqueue(new Callback<AuthApi.AuthResponse>() {
            @Override
            public void onResponse(
                    Call<AuthApi.AuthResponse> call,
                    Response<AuthApi.AuthResponse> response
            ) {
                AuthApi.AuthResponse body = response.body();

                if (!response.isSuccessful() || body == null) {
                    Toast.makeText(
                            RegisterActivity.this,
                            "Registration failed. Please check your details and try again.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                session.cacheEmail(emailAddress);
                session.cacheProfile(fullName, "voter", false);

                Toast.makeText(
                        RegisterActivity.this,
                        "Account created. Check your email for the verification link, then log in.",
                        Toast.LENGTH_LONG
                ).show();

                open(LoginActivity.class);
                finish();

                Toast.makeText(
                        RegisterActivity.this,
                        "Account created. Check your email for the verification link.",
                        Toast.LENGTH_LONG
                ).show();

                open(LoginActivity.class);
                finish();
            }

            @Override
            public void onFailure(
                    Call<AuthApi.AuthResponse> call,
                    Throwable t
            ) {
                Toast.makeText(
                        RegisterActivity.this,
                        "Network error. Check your internet connection.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
