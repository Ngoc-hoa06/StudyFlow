package com.example.studyflow.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.studyflow.LoginActivity;
import com.example.studyflow.R;
import com.example.studyflow.data.entity.Task;
import com.example.studyflow.data.model.UserProfile;
import com.example.studyflow.data.repository.UserProfileRepository;
import com.example.studyflow.viewmodel.FocusSessionViewModel;
import com.example.studyflow.viewmodel.TaskViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView tvUserEmail;
    private TextView tvTotalFocusTime;
    private TextView tvFocusSessions;
    private TextView tvCompletedTasks;

    private TextInputEditText etName;
    private TextInputEditText etUniversity;
    private TextInputEditText etMajor;
    private TextInputEditText etYear;
    private TextInputEditText etBio;

    private MaterialButton btnSaveProfile;
    private MaterialButton btnLogout;

    private FocusSessionViewModel focusSessionViewModel;
    private TaskViewModel taskViewModel;

    private UserProfileRepository userProfileRepository;

    private FirebaseUser currentUser;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_profile,
                container,
                false
        );

        // User information
        tvUserEmail =
                view.findViewById(
                        R.id.tvUserEmail
                );

        etName =
                view.findViewById(
                        R.id.etName
                );

        etUniversity =
                view.findViewById(
                        R.id.etUniversity
                );

        etMajor =
                view.findViewById(
                        R.id.etMajor
                );

        etYear =
                view.findViewById(
                        R.id.etYear
                );

        etBio =
                view.findViewById(
                        R.id.etBio
                );

        btnSaveProfile =
                view.findViewById(
                        R.id.btnSaveProfile
                );

        btnLogout =
                view.findViewById(
                        R.id.btnLogout
                );

        // Statistics
        tvTotalFocusTime =
                view.findViewById(
                        R.id.tvTotalFocusTime
                );

        tvFocusSessions =
                view.findViewById(
                        R.id.tvFocusSessions
                );

        tvCompletedTasks =
                view.findViewById(
                        R.id.tvCompletedTasks
                );

        // Firebase
        currentUser =
                FirebaseAuth
                        .getInstance()
                        .getCurrentUser();

        userProfileRepository =
                new UserProfileRepository();

        setupUser();

        loadUserProfile();

        // ViewModels
        focusSessionViewModel =
                new ViewModelProvider(this)
                        .get(
                                FocusSessionViewModel.class
                        );

        taskViewModel =
                new ViewModelProvider(this)
                        .get(
                                TaskViewModel.class
                        );

        observeStatistics();

        btnSaveProfile.setOnClickListener(v ->
                saveProfile()
        );

        btnLogout.setOnClickListener(v ->
                logout()
        );

        return view;
    }

    private void setupUser() {

        if (currentUser != null
                && currentUser.getEmail() != null) {

            tvUserEmail.setText(
                    currentUser.getEmail()
            );

        } else {

            tvUserEmail.setText(
                    "No user information"
            );
        }
    }

    private void loadUserProfile() {

        if (currentUser == null) {
            return;
        }

        userProfileRepository
                .getUserProfile(
                        currentUser.getUid(),
                        new UserProfileRepository
                                .OnProfileLoadedListener() {

                            @Override
                            public void onSuccess(
                                    UserProfile profile) {

                                if (!isAdded()
                                        || profile == null) {
                                    return;
                                }

                                etName.setText(
                                        safeText(
                                                profile.getName()
                                        )
                                );

                                etUniversity.setText(
                                        safeText(
                                                profile.getUniversity()
                                        )
                                );

                                etMajor.setText(
                                        safeText(
                                                profile.getMajor()
                                        )
                                );

                                etYear.setText(
                                        safeText(
                                                profile.getYear()
                                        )
                                );

                                etBio.setText(
                                        safeText(
                                                profile.getBio()
                                        )
                                );
                            }

                            @Override
                            public void onNotFound() {

                                if (!isAdded()) {
                                    return;
                                }

                                // Profile document does not exist yet.
                                // Fill name from Google/Firebase if available.

                                if (currentUser
                                        .getDisplayName() != null) {

                                    etName.setText(
                                            currentUser
                                                    .getDisplayName()
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Exception e) {

                                if (!isAdded()) {
                                    return;
                                }

                                Toast.makeText(
                                        requireContext(),
                                        "Could not load profile",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private void saveProfile() {

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "User is not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String name =
                getText(etName);

        String university =
                getText(etUniversity);

        String major =
                getText(etMajor);

        String year =
                getText(etYear);

        String bio =
                getText(etBio);

        String email =
                currentUser.getEmail() != null
                        ? currentUser.getEmail()
                        : "";

        String avatarUrl =
                currentUser.getPhotoUrl() != null
                        ? currentUser
                        .getPhotoUrl()
                        .toString()
                        : "";

        UserProfile profile =
                new UserProfile(
                        currentUser.getUid(),
                        name,
                        email,
                        avatarUrl,
                        university,
                        major,
                        year,
                        bio
                );

        btnSaveProfile.setEnabled(false);

        btnSaveProfile.setText(
                "Saving..."
        );

        userProfileRepository
                .saveUserProfile(
                        profile,
                        new UserProfileRepository
                                .OnProfileSavedListener() {

                            @Override
                            public void onSuccess() {

                                if (!isAdded()) {
                                    return;
                                }

                                btnSaveProfile
                                        .setEnabled(true);

                                btnSaveProfile
                                        .setText(
                                                "Save Profile"
                                        );

                                Toast.makeText(
                                        requireContext(),
                                        "Profile saved successfully",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                            @Override
                            public void onFailure(
                                    Exception e) {

                                if (!isAdded()) {
                                    return;
                                }

                                btnSaveProfile
                                        .setEnabled(true);

                                btnSaveProfile
                                        .setText(
                                                "Save Profile"
                                        );

                                Toast.makeText(
                                        requireContext(),
                                        "Failed to save profile",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private String getText(
            TextInputEditText editText) {

        if (editText.getText() == null) {
            return "";
        }

        return editText
                .getText()
                .toString()
                .trim();
    }

    private String safeText(
            String value) {

        return value == null
                ? ""
                : value;
    }

    private void observeStatistics() {

        focusSessionViewModel
                .getTotalFocusMinutes()
                .observe(
                        getViewLifecycleOwner(),
                        minutes -> {

                            int total =
                                    minutes == null
                                            ? 0
                                            : minutes;

                            if (total >= 60) {

                                int hours =
                                        total / 60;

                                int remainingMinutes =
                                        total % 60;

                                tvTotalFocusTime
                                        .setText(
                                                hours
                                                        + " h "
                                                        + remainingMinutes
                                                        + " min"
                                        );

                            } else {

                                tvTotalFocusTime
                                        .setText(
                                                total
                                                        + " min"
                                        );
                            }
                        }
                );

        focusSessionViewModel
                .getAllSessions()
                .observe(
                        getViewLifecycleOwner(),
                        sessions -> {

                            int count =
                                    sessions == null
                                            ? 0
                                            : sessions.size();

                            tvFocusSessions
                                    .setText(
                                            String.valueOf(
                                                    count
                                            )
                                    );
                        }
                );

        taskViewModel
                .getAllTasks()
                .observe(
                        getViewLifecycleOwner(),
                        tasks -> {

                            int completedCount = 0;

                            if (tasks != null) {

                                for (Task task : tasks) {

                                    if ("COMPLETED"
                                            .equals(
                                                    task.getStatus()
                                            )) {

                                        completedCount++;
                                    }
                                }
                            }

                            tvCompletedTasks
                                    .setText(
                                            String.valueOf(
                                                    completedCount
                                            )
                                    );
                        }
                );
    }

    private void logout() {

        FirebaseAuth
                .getInstance()
                .signOut();

        Intent intent =
                new Intent(
                        requireContext(),
                        LoginActivity.class
                );

        startActivity(intent);

        requireActivity().finish();
    }
}