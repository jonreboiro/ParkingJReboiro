package com.lksnext.parkingJReboiro.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<Boolean> _isAuthenticated = new MutableLiveData<>(false);
    public LiveData<Boolean> isAuthenticated = _isAuthenticated;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final FirebaseAuth firebaseAuth;

    public MainViewModel() {
        // Inicializamos Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Comprobamos estado de autenticación al inicio
        checkAuthenticationState();
    }

    /**
     * Comprueba si el usuario está autenticado
     */
    public void checkAuthenticationState() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        _isAuthenticated.setValue(currentUser != null);
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public void signOut() {
        _isLoading.setValue(true);
        firebaseAuth.signOut();
        _isAuthenticated.setValue(false);
        _isLoading.setValue(false);
    }

    /**
     * Refresca datos de usuario si es necesario
     */
    public void refreshUserData() {
        // Aquí podríamos cargar datos adicionales del usuario
        // como nombre, permisos, etc.
        checkAuthenticationState();
    }

    /**
     * Limpia mensajes de error
     */
    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}