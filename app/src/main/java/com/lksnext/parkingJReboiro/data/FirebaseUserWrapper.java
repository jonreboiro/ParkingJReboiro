package com.lksnext.parkingJReboiro.data;

import com.google.firebase.auth.FirebaseUser;

public class FirebaseUserWrapper implements IFirebaseUser {
    private final FirebaseUser firebaseUser;

    public FirebaseUserWrapper(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    @Override
    public String getEmail() {
        return firebaseUser.getEmail();
    }

    @Override
    public String getUid() {
        return firebaseUser.getUid();
    }

    // Implementa otros métodos según necesites
}