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

        if (name.getText().toString().trim().length() < 3) { name.setError("Enter your full name"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString().trim()).matches()) { email.setError("Enter a valid email"); return; }
        if (id.getText().toString().length() != 13) { id.setError("A South African ID number has 13 digits"); return; }
        if (password.getText().toString().length() < 8) { password.setError("Use at least 8 characters"); return; }
        if (!password.getText().toString().equals(confirm.getText().toString())) { confirm.setError("Passwords do not match"); return; }
        if (!consent.isChecked()) { consent.setError("Consent is required for verification"); return; }

        Map<String, String> meta = new HashMap<>();
        meta.put("full_name", name.getText().toString().trim());
        meta.put("phone", phone.getText().toString().trim());
        meta.put("sa_id_number", id.getText().toString().trim());

        SessionManager session = new SessionManager(this);
        AuthApi api = ApiClient.get(session).create(AuthApi.class);
        api.signUp(new AuthApi.SignUpRequest(email.getText().toString().trim(), password.getText().toString(), meta))
                .enqueue(new Callback<AuthApi.AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthApi.AuthResponse> call, Response<AuthApi.AuthResponse> resp) {
                        AuthApi.AuthResponse body = resp.body();
                        if (!resp.isSuccessful() || body == null || body.accessToken == null) {
                            String msg = body != null && body.message != null ? body.message : "Registration failed";
                            Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                            return;
                        }
                        session.saveSession(body.user.id, body.accessToken, body.refreshToken);
                        session.cacheEmail(email.getText().toString().trim());
                        session.cacheProfile(name.getText().toString().trim(), "voter", false);
                        open(VerificationActivity.class);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<AuthApi.AuthResponse> call, Throwable t) {
                        Toast.makeText(RegisterActivity.this, "Network error. Check your connection.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}