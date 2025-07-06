package com.lksnext.parkingJReboiro.viewmodel;

import android.app.Application;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lksnext.parkingJReboiro.data.DataRepository;
import com.lksnext.parkingJReboiro.domain.Callback;

public class LoginViewModel extends AndroidViewModel {

    private final DataRepository repository;
    private final MutableLiveData<Boolean> isLogged = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> needProfileCompletion = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = DataRepository.getInstance();
    }

    public LiveData<Boolean> isLogged() {
        return isLogged;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getEmailError() {
        return emailError;
    }

    public LiveData<String> getPasswordError() {
        return passwordError;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getNeedProfileCompletion() {
        return needProfileCompletion;
    }

    public boolean validarFormulario(String email, String password) {
        boolean esValido = true;

        if (email.isEmpty()) {
            emailError.setValue("Campo obligatorio");
            esValido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setValue("Email no válido");
            esValido = false;
        } else {
            emailError.setValue(null);
        }

        if (password.isEmpty()) {
            passwordError.setValue("Campo obligatorio");
            esValido = false;
        } else {
            passwordError.setValue(null);
        }

        return esValido;
    }

    public void loginUsuario(String email, String password) {
        if (validarFormulario(email, password)) {
            isLoading.setValue(true);
            repository.loginWithEmailAndPassword(email, password, new Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    isLoading.setValue(false);
                    isLogged.setValue(true);
                }

                @Override
                public void onError(String message) {
                    isLoading.setValue(false);
                    errorMessage.setValue(message);
                    isLogged.setValue(false);
                }
            });
        }
    }

    public void firebaseAuthWithGoogle(String idToken) {
        isLoading.setValue(true);
        repository.loginWithGoogle(idToken, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean needsProfileCompletion) {
                isLoading.setValue(false);
                if (needsProfileCompletion) {
                    needProfileCompletion.setValue(true);
                } else {
                    isLogged.setValue(true);
                }
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
                isLogged.setValue(false);
            }
        });
    }
}