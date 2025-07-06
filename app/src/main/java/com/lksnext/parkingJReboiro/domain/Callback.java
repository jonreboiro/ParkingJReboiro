package com.lksnext.parkingJReboiro.domain;

public interface Callback<T> {
    void onSuccess(T result);
    void onError(String message);
}