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


        if (fullName.length() < 3) {
            name.setError("Enter your full name");
            return;
        }

        // check that the email address is entered in the correct format
        if (!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            email.setError("Enter a valid email");
            return;
        }

        if (!idNumber.matches("\\d{13}")) {
            id.setError("Enter a 13-digit South African ID number");
            return;
        }

        if (dobText.isEmpty()) {
            dob.setError("Select your date of birth");
            Toast.makeText(
                    this,
                    "Select your date of birth",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // only allow users who are at least 18 years old to register
        if (!isOldEnoughToVote(dobText)) {
            dob.setError("You must be 18 or older to register");
            Toast.makeText(
                    this,
                    "You must be 18 or older to register",
                    Toast.LENGTH_LONG
            ).show();
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

        // add the user's extra details to the sign-up request
        Map<String, String> metadata = new HashMap<>();
        metadata.put("full_name", fullName);
        metadata.put("phone", phoneNumber);
        metadata.put("sa_id_number", idNumber);
        metadata.put("date_of_birth", dobText);

        // set up the session manager and authentication service
        SessionManager session = new SessionManager(this);
        AuthApi api = ApiClient.get(session).create(AuthApi.class);

        // prepare the details that Supabase needs to create the account
        AuthApi.SignUpRequest request =
                new AuthApi.SignUpRequest(
                        emailAddress,
                        passwordValue,
                        metadata
                );

        // send the registration request to Supabase
        api.signUp(request).enqueue(new Callback<AuthApi.AuthResponse>() {

            @Override
            public void onResponse(
                    Call<AuthApi.AuthResponse> call,
                    Response<AuthApi.AuthResponse> response
            ) {
                // get the response returned by Supabase
                AuthApi.AuthResponse body = response.body();

                // show an error if the registration request was unsuccessful
                if (!response.isSuccessful() || body == null) {
                    Toast.makeText(
                            RegisterActivity.this,
                            "Registration failed. Please check your details and try again.",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                // save the email and basic profile details for later use
                session.cacheEmail(emailAddress);
                session.cacheProfile(fullName, "voter", false);

                // tell the user to verify their email before logging in THIS IS A SECTION TO UPDATE LATER ON
                Toast.makeText(
                        RegisterActivity.this,
                        "Account created. Check your email for the verification link, then log in.",
                        Toast.LENGTH_LONG
                ).show();

                // take the user to the login screen after registration
                open(LoginActivity.class);
                finish();
            }

            @Override
            public void onFailure(
                    Call<AuthApi.AuthResponse> call,
                    Throwable t
            ) {
                // let the user know if the request fails because of a connection problem
                Toast.makeText(
                        RegisterActivity.this,
                        "Network error. Check your internet connection.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showDatePicker(EditText dob) {


        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -18);

        // create the date picker and display the selected date in the input field
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {

                    // save the date in yyyy-MM-dd format for validation and storage
                    dob.setText(String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                    ));

                    // clear the error once a date has been selected
                    dob.setError(null);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );

        // prevent the user from selecting a date in the future
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        dialog.show();
    }

    // check the user's date of birth to see if they are at least 18
    private boolean isOldEnoughToVote(String dobText) {
        try {
            // read the year, month, and day from the selected date
            int year = Integer.parseInt(dobText.substring(0, 4));
            int month = Integer.parseInt(dobText.substring(5, 7));
            int day = Integer.parseInt(dobText.substring(8, 10));

            // create a calendar using the user's date of birth
            Calendar birthDate = Calendar.getInstance();
            birthDate.clear();
            birthDate.setLenient(false);
            birthDate.set(year, month - 1, day);

            birthDate.getTime();


            birthDate.add(Calendar.YEAR, 18);

            // get today's date and remove the time
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
