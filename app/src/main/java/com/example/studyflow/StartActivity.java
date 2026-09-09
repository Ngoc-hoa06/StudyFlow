package com.example.studyflow;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.button.MaterialButton;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            openMain();
            return;
        }

        setContentView(R.layout.activity_start);

        MaterialButton createAccountButton = findViewById(R.id.btnStartCreateAccount);
        MaterialButton loginButton = findViewById(R.id.btnStartLogin);

        createAccountButton.setOnClickListener(v -> openAuth(RegisterActivity.class));
        loginButton.setOnClickListener(v -> openAuth(LoginActivity.class));
    }

    private void openAuth(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
        finish();
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
