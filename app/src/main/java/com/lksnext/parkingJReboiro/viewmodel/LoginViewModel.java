package com.lksnext.parkingJReboiro.viewmodel;

import android.app.Application;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginViewModel extends AndroidViewModel {

    private final FirebaseAuth mAuth;
    private final MutableLiveData<Boolean> isLogged = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LoginViewModel(@NonNull Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
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
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        isLoading.setValue(false);
                        if (task.isSuccessful()) {
                            isLogged.setValue(Boolean.TRUE);
                            if (task.isSuccessful()) {
                                isLogged.setValue(Boolean.TRUE);
                            }
                        } else {
                            errorMessage.setValue(obtenerMensajeError(task.getException()));
                            isLogged.setValue(Boolean.FALSE);
                        }
                    });
        }
    }

    public void firebaseAuthWithGoogle(String idToken) {
        isLoading.setValue(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (task.isSuccessful()) {
                        Log.d("Login", "UID: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
                        isLogged.setValue(Boolean.TRUE);
                    } else {
                        errorMessage.setValue("Error autenticando con Google");
                        isLogged.setValue(Boolean.FALSE);
                    }
                });
    }

    private String obtenerMensajeError(Exception e) {
        if (e != null && e.getMessage() != null) {
            if (e.getMessage().contains("There is no user record")) {
                return "Usuario no registrado";
            }
            if (e.getMessage().contains("The password is invalid")) {
                return "Contraseña incorrecta";
            }
        }
        return "Error al iniciar sesión: " + (e != null ? e.getMessage() : "");
    }
}