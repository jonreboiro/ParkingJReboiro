package com.lksnext.parkingJReboiro.data;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Collections;
import java.util.List;

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

    @Override
    public List<? extends UserInfo> getProviderData() {
        return firebaseUser.getProviderData();
    }

    // Implementa otros métodos según necesites
}