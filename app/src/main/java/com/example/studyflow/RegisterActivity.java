package com.example.studyflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.studyflow.data.model.UserProfile;
import com.example.studyflow.data.repository.UserProfileRepository;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        MaterialButton btnRegister =
                findViewById(R.id.btnRegister);

        TextView tvBackToLogin =
                findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(v ->
                registerUser()
        );

        tvBackToLogin.setOnClickListener(v ->
                finish()
        );
    }

    private void registerUser() {

        String email =
                etEmail.getText().toString().trim();

        String password =
                etPassword.getText().toString().trim();

        String confirmPassword =
                etConfirmPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(
                    "Password must contain at least 6 characters"
            );
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(
                    "Passwords do not match"
            );
            return;
        }

        firebaseAuth
                .createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser =
                                FirebaseAuth.getInstance()
                                        .getCurrentUser();

                        if (firebaseUser == null) {

                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Registration failed",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        String uid =
                                firebaseUser.getUid();

                        String userEmail =
                                firebaseUser.getEmail() != null
                                        ? firebaseUser.getEmail()
                                        : email;

                        UserProfile profile =
                                new UserProfile(
                                        uid,
                                        "",
                                        userEmail,
                                        "",
                                        "",
                                        "",
                                        "",
                                        ""
                                );

                        UserProfileRepository repository =
                                new UserProfileRepository();

                        repository.createProfileIfNotExists(
                                profile,
                                new UserProfileRepository
                                        .OnProfileSavedListener() {

                                    @Override
                                    public void onSuccess() {

                                        Toast.makeText(
                                                RegisterActivity.this,
                                                "Registration successful",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        Intent intent =
                                                new Intent(
                                                        RegisterActivity.this,
                                                        MainActivity.class
                                                );

                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {

                                        Toast.makeText(
                                                RegisterActivity.this,
                                                "Account created, but profile could not be saved",
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                }
                        );
                    }
                });
    }
}