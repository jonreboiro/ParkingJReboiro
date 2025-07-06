package com.lksnext.parkingJReboiro.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingJReboiro.data.DataRepository;
import com.lksnext.parkingJReboiro.domain.Callback;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<Boolean> _isAuthenticated = new MutableLiveData<>(false);
    public LiveData<Boolean> isAuthenticated = _isAuthenticated;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final DataRepository dataRepository;

    public MainViewModel() {
        // Obtenemos la instancia del repositorio
        dataRepository = DataRepository.getInstance();

        // Comprobamos estado de autenticación al inicio
        checkAuthenticationState();
    }

    /**
     * Comprueba si el usuario está autenticado
     */
    public void checkAuthenticationState() {
        _isAuthenticated.setValue(dataRepository.getCurrentUser() != null);
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public void signOut() {
        _isLoading.setValue(true);
        dataRepository.signOut(new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _isAuthenticated.setValue(false);
                _isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                _errorMessage.setValue(message);
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Refresca datos de usuario si es necesario
     */
    public void refreshUserData() {
        checkAuthenticationState();
    }

    /**
     * Limpia mensajes de error
     */
    public void clearErrorMessage() {
        _errorMessage.setValue(null);
    }
}