package com.lksnext.parkingJReboiro.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingJReboiro.data.DataRepository;
import com.lksnext.parkingJReboiro.data.IDataRepository;
import com.lksnext.parkingJReboiro.domain.Callback;

public class RegisterViewModel extends ViewModel {

    private final IDataRepository repository;

    public RegisterViewModel(IDataRepository repository) {
        this.repository = repository;
    }

    public RegisterViewModel() {
        this.repository = DataRepository.getInstance();
    }

    // LiveData para errores de validación
    private final MutableLiveData<String> usernameError = new MutableLiveData<>();
    private final MutableLiveData<String> emailError = new MutableLiveData<>();
    private final MutableLiveData<String> employeeIdError = new MutableLiveData<>();
    private final MutableLiveData<String> passwordError = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPasswordError = new MutableLiveData<>();

    // LiveData para el estado del registro
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Getters para los LiveData
    public LiveData<String> getUsernameError() { return usernameError; }
    public LiveData<String> getEmailError() { return emailError; }
    public LiveData<String> getEmployeeIdError() { return employeeIdError; }
    public LiveData<String> getPasswordError() { return passwordError; }
    public LiveData<String> getConfirmPasswordError() { return confirmPasswordError; }
    public LiveData<Boolean> isLoading() { return isLoading; }
    public LiveData<Boolean> getRegistrationSuccess() { return registrationSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public boolean validarFormulario(String username, String email, String employeeId, String password, String confirmPassword) {
        boolean esValido = true;

        if (username.trim().isEmpty()) {
            usernameError.setValue("Campo obligatorio");
            esValido = false;
        } else {
            usernameError.setValue(null);
        }

        if (email.isEmpty()) {
            emailError.setValue("Campo obligatorio");
            esValido = false;
        } else if (!isEmailValid(email)) {
            emailError.setValue("Email no válido");
            esValido = false;
        } else {
            emailError.setValue(null);
        }

        if (employeeId.trim().isEmpty()) {
            employeeIdError.setValue("Campo obligatorio");
            esValido = false;
        } else {
            employeeIdError.setValue(null);
        }

        if (!esPasswordValida(password)) {
            passwordError.setValue("Mín. 8 caracteres, mayúscula, minúscula y número");
            esValido = false;
        } else {
            passwordError.setValue(null);
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordError.setValue("Las contraseñas no coinciden");
            esValido = false;
        } else {
            confirmPasswordError.setValue(null);
        }

        return esValido;
    }

    private static boolean isEmailValid(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean esPasswordValida(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
    }

    public void registrarUsuario(String username, String email, String employeeId, String phone, String password) {
        isLoading.setValue(true);

        repository.registerUser(username, email, employeeId, phone, password, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                isLoading.setValue(false);
                registrationSuccess.setValue(true);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}