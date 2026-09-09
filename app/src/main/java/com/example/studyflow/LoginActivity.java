package com.example.studyflow;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.studyflow.data.model.UserProfile;
import com.example.studyflow.data.repository.UserProfileRepository;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;

    private FirebaseAuth firebaseAuth;
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        firebaseAuth =
                FirebaseAuth.getInstance();

        credentialManager =
                CredentialManager.create(this);

        etEmail =
                findViewById(R.id.etEmail);

        etPassword =
                findViewById(R.id.etPassword);

        MaterialButton btnLogin =
                findViewById(R.id.btnLogin);

        MaterialButton btnGoogleSignIn =
                findViewById(R.id.btnGoogleSignIn);

        TextView tvRegister =
                findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v ->
                loginUser()
        );

        btnGoogleSignIn.setOnClickListener(v ->
                signInWithGoogle()
        );

        tvRegister.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            LoginActivity.this,
                            RegisterActivity.class
                    );

            startActivity(intent);
        });
    }

    private void loginUser() {

        String email =
                etEmail.getText()
                        .toString()
                        .trim();

        String password =
                etPassword.getText()
                        .toString()
                        .trim();

        if (email.isEmpty()) {

            etEmail.setError(
                    "Email is required"
            );

            return;
        }

        if (password.isEmpty()) {

            etPassword.setError(
                    "Password is required"
            );

            return;
        }

        firebaseAuth
                .signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Intent intent =
                                new Intent(
                                        LoginActivity.this,
                                        MainActivity.class
                                );

                        startActivity(intent);
                        finish();

                    } else {

                        String message =
                                task.getException() != null
                                        ? task.getException().getMessage()
                                        : "Unknown login error";

                        Toast.makeText(
                                LoginActivity.this,
                                "Login failed: " + message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void signInWithGoogle() {

        GetGoogleIdOption googleIdOption =
                new GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(
                                getString(
                                        R.string.default_web_client_id
                                )
                        )
                        .build();

        GetCredentialRequest request =
                new GetCredentialRequest.Builder()
                        .addCredentialOption(
                                googleIdOption
                        )
                        .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new androidx.credentials.CredentialManagerCallback
                        <GetCredentialResponse,
                                GetCredentialException>() {

                    @Override
                    public void onResult(
                            GetCredentialResponse result) {

                        runOnUiThread(() ->
                                handleGoogleCredential(
                                        result.getCredential()
                                )
                        );
                    }

                    @Override
                    public void onError(
                            GetCredentialException e) {

                        runOnUiThread(() ->

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Google sign-in failed: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
        );
    }

    private void handleGoogleCredential(
            Credential credential) {

        if (!(credential
                instanceof CustomCredential)) {

            Toast.makeText(
                    LoginActivity.this,
                    "Unsupported Google credential",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        CustomCredential customCredential =
                (CustomCredential) credential;

        if (!GoogleIdTokenCredential
                .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                .equals(
                        customCredential.getType()
                )) {

            Toast.makeText(
                    LoginActivity.this,
                    "Invalid Google credential",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        GoogleIdTokenCredential googleCredential =
                GoogleIdTokenCredential
                        .createFrom(
                                customCredential.getData()
                        );

        firebaseAuthWithGoogle(
                googleCredential.getIdToken()
        );
    }

    private void firebaseAuthWithGoogle(
            String idToken) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(
                        idToken,
                        null
                );

        firebaseAuth
                .signInWithCredential(
                        credential
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            if (!task.isSuccessful()) {

                                String errorMessage =
                                        task.getException() != null
                                                ? task
                                                .getException()
                                                .getMessage()
                                                : "Unknown authentication error";

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Firebase Google login failed: "
                                                + errorMessage,
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            FirebaseUser user =
                                    firebaseAuth
                                            .getCurrentUser();

                            if (user == null) {

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Google login failed: user not found",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            createProfileIfNeeded(
                                    user
                            );

                            Intent intent =
                                    new Intent(
                                            LoginActivity.this,
                                            MainActivity.class
                                    );

                            startActivity(intent);
                            finish();
                        }
                );
    }

    private void createProfileIfNeeded(
            FirebaseUser user) {

        String uid =
                user.getUid();

        String name =
                user.getDisplayName() != null
                        ? user.getDisplayName()
                        : "";

        String email =
                user.getEmail() != null
                        ? user.getEmail()
                        : "";

        String avatarUrl =
                user.getPhotoUrl() != null
                        ? user
                        .getPhotoUrl()
                        .toString()
                        : "";

        UserProfile profile =
                new UserProfile(
                        uid,
                        name,
                        email,
                        avatarUrl,
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

                        android.util.Log.d(
                                "StudyFlowFirestore",
                                "User profile saved successfully"
                        );
                    }

                    @Override
                    public void onFailure(
                            Exception e) {

                        android.util.Log.e(
                                "StudyFlowFirestore",
                                "Failed to save user profile",
                                e
                        );
                    }
                }
        );
    }
}