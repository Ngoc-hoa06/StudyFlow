package com.example.studyflow.data.repository;

import com.example.studyflow.data.model.UserProfile;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileRepository {

    private final FirebaseFirestore firestore;

    public UserProfileRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void saveUserProfile(
            UserProfile profile,
            OnProfileSavedListener listener
    ) {

        firestore
                .collection("users")
                .document(profile.getUid())
                .set(profile)
                .addOnSuccessListener(unused -> {
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    listener.onFailure(e);
                });
    }

    public void getUserProfile(
            String uid,
            OnProfileLoadedListener listener
    ) {

        firestore
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        UserProfile profile =
                                documentSnapshot.toObject(
                                        UserProfile.class
                                );

                        listener.onSuccess(profile);

                    } else {

                        listener.onNotFound();
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onFailure(e);
                });
    }

    public interface OnProfileSavedListener {

        void onSuccess();

        void onFailure(Exception e);
    }

    public interface OnProfileLoadedListener {

        void onSuccess(UserProfile profile);

        void onNotFound();

        void onFailure(Exception e);
    }
    public void createProfileIfNotExists(
            UserProfile profile,
            OnProfileSavedListener listener
    ) {

        firestore
                .collection("users")
                .document(profile.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        listener.onSuccess();

                    } else {

                        firestore
                                .collection("users")
                                .document(profile.getUid())
                                .set(profile)
                                .addOnSuccessListener(unused ->
                                        listener.onSuccess()
                                )
                                .addOnFailureListener(
                                        listener::onFailure
                                );
                    }
                })
                .addOnFailureListener(
                        listener::onFailure
                );
    }
}