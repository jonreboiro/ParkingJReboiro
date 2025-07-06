package com.lksnext.parkingJReboiro.data;

public interface IFirebaseAuthProvider {
    IFirebaseUser getCurrentUser();
    String getUid();
}