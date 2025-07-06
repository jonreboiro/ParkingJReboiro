package com.lksnext.parkingJReboiro.data;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseAuthProvider implements IFirebaseAuthProvider {
    private final FirebaseAuth mAuth;

    public FirebaseAuthProvider() {
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public IFirebaseUser getCurrentUser() {
        if (mAuth.getCurrentUser() == null) return null;
        return new FirebaseUserWrapper(mAuth.getCurrentUser());
    }

    @Override
    public String getUid() {
        return mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
    }
}