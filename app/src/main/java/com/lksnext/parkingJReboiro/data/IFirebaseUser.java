package com.lksnext.parkingJReboiro.data;

import com.google.firebase.auth.UserInfo;

import java.util.List;

public interface IFirebaseUser {
    String getEmail();
    String getUid();
    List<? extends UserInfo> getProviderData();

}