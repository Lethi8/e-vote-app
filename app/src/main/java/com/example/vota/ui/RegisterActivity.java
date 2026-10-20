package com.example.vota.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vota.R;
import com.example.vota.net.ApiClient;
import com.example.vota.net.AuthApi;
import com.example.vota.util.SessionManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText dobField = findViewById(R.id.edtDob);
        dobField.setOnClickListener(v -> showDatePicker(dobField));

        findViewById(R.id.btnCreate).setOnClickListener(v -> create());
    }

    // Runs every validation in sequence; each returns immediately on the
    // first failure, so only one error shows at a time.
    private void create() {
        EditText name = findViewById(R.id.edtName);
        EditText email = findViewById(R.id.edtEmail);
        EditText phone = findViewById(R.id.edtPhone);
        EditText id = findViewById(R.id.edtId);
        EditText dob = findViewById(R.id.edtDob);
        EditText password = findViewById(R.id.edtPassword);
        EditText confirm = findViewById(R.id.edtConfirm);
        CheckBox consent = findViewById(R.id.chkConsent);

        String fullName = name.getText().toString().trim();
        String emailAddress = email.getText().toString().trim();
        String phoneNumber = phone.getText().toString().trim();
        String idNumber = id.getText().toString().trim();
        String dobText = dob.getText().toString().trim();
        String passwordValue = password.getText().toString();

        if (fullName.length() < 3) { name.setError("Enter your full name"); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) { email.setError("Enter a valid email"); return; }
        if (!idNumber.matches("\\d{13}")) { id.setError("Enter a 13-digit South African ID number"); return; }

        if (dobText.isEmpty()) {
            dob.setError("Select your date of birth");
            Toast.makeText(this, "Select your date of birth", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isOldEnoughToVote(dobText)) {
            dob.setError("You must be 18 or older to register");
            Toast.makeText(this, "You must be 18 or older to register", Toast.LENGTH_LONG).show();
            return;
        }

        if (passwordValue.length() < 8) { password.setError("Use at least 8 characters"); return; }
        if (!passwordValue.equals(confirm.getText().toString())) { confirm.setError("Passwords do not match"); return; }
        if (!consent.isChecked()) { consent.setError("Consent is required for verification"); return; }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("full_name", fullName);
        metadata.put("phone", phoneNumber);
        metadata.put("sa_id_number", idNumber);
        metadata.put("date_of_birth", dobText);

        SessionManager session = new SessionManager(this);
        AuthApi api = ApiClient.get(session).create(AuthApi.class);

        api.signUp(new AuthApi.SignUpRequest(emailAddress, passwordValue, metadata))
                .enqueue(new Callback<AuthApi.AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthApi.AuthResponse> call, Response<AuthApi.AuthResponse> response) {
                        AuthApi.AuthResponse body = response.body();

                        if (!response.isSuccessful() || body == null) {
                            // Prefer the server's actual reason when it gives one.
                            String msg = body != null && body.message != null
                                    ? body.message
                                    : "Registration failed. Please check your details and try again.";
                            Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                            return;
                        }

                        session.cacheEmail(emailAddress);
                        session.cacheProfile(fullName, "voter", false);

                        Toast.makeText(RegisterActivity.this,
                                "Account created. Check your email for the verification link, then log in.",
                                Toast.LENGTH_LONG).show();

                        open(LoginActivity.class);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<AuthApi.AuthResponse> call, Throwable t) {
                        Toast.makeText(RegisterActivity.this, "Network error. Check your internet connection.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showDatePicker(EditText dob) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -18);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    dob.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day));
                    dob.setError(null);
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private boolean isOldEnoughToVote(String dobText) {
        try {
            int year = Integer.parseInt(dobText.substring(0, 4));
            int month = Integer.parseInt(dobText.substring(5, 7));
            int day = Integer.parseInt(dobText.substring(8, 10));

            Calendar birthDate = Calendar.getInstance();
            birthDate.clear();
            birthDate.setLenient(false);
            birthDate.set(year, month - 1, day);
            birthDate.getTime();
            birthDate.add(Calendar.YEAR, 18);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            return !birthDate.after(today);
        } catch (Exception e) {
            return false;
        }
    }
}