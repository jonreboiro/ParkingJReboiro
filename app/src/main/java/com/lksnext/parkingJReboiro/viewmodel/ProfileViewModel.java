package com.lksnext.parkingJReboiro.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lksnext.parkingJReboiro.data.DataRepository;
import com.lksnext.parkingJReboiro.domain.Callback;
import com.lksnext.parkingJReboiro.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ProfileViewModel extends ViewModel {

    private final DataRepository repository;
    private final FirebaseAuth mAuth;

    private MutableLiveData<User> userData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> navigateToLogin = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private MutableLiveData<Boolean> passwordChangeSuccess = new MutableLiveData<>();
    private MutableLiveData<List<String>> matriculasList = new MutableLiveData<>(new ArrayList<>());

    public ProfileViewModel() {
        repository = DataRepository.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Verificar si hay usuario logueado
        if (mAuth.getCurrentUser() == null) {
            navigateToLogin.setValue(true);
        } else {
            loadUserData();
        }
    }

    public void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin.setValue(true);
            return;
        }

        isLoading.setValue(true);
        String uid = currentUser.getUid();

        repository.getUserProfile(uid).observeForever(user -> {
            if (user != null) {
                userData.setValue(user);
                if (user.getMatriculas() != null) {
                    matriculasList.setValue(user.getMatriculas());
                }
            }
            isLoading.setValue(false);
        });
    }

    public void updateUserProfile(String username, String email, String phone, String employeeId) {
        if (username.isEmpty()) {
            errorMessage.setValue("El nombre no puede estar vacío");
            return;
        }

        if (!isValidPhone(phone)) {
            errorMessage.setValue("Teléfono no válido");
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin.setValue(true);
            return;
        }

        isLoading.setValue(true);

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setEmployeeId(employeeId);
        user.setMatriculas(matriculasList.getValue());

        repository.updateUserProfile(user, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateSuccess.setValue(true);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue("Error al guardar cambios: " + message);
                isLoading.setValue(false);
            }
        });
    }

    public void changePassword(String newPassword, String currentPassword) {
        if (!isPasswordValid(newPassword)) {
            errorMessage.setValue("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número");
            return;
        }

        isLoading.setValue(true);

        repository.changePassword(currentPassword, newPassword, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                passwordChangeSuccess.setValue(true);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                passwordChangeSuccess.setValue(false);
                isLoading.setValue(false);
            }
        });
    }

    public void addPlate(String matricula) {
        if (!esMatriculaEspanolaValida(matricula)) {
            errorMessage.setValue("Formato de matrícula inválido. Debe ser 4 números seguidos de 3 consonantes mayúsculas.");
            return;
        }

        isLoading.setValue(true);

        repository.addMatricula(matricula, new Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> updatedList) {
                matriculasList.setValue(updatedList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void removePlate(String matricula) {
        List<String> currentList = matriculasList.getValue();
        if (currentList == null || !currentList.contains(matricula)) {
            return;
        }

        isLoading.setValue(true);

        repository.removeMatricula(matricula, new Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> updatedList) {
                matriculasList.setValue(updatedList);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue("Error al eliminar la matrícula: " + message);
                isLoading.setValue(false);
            }
        });
    }

    public void logout() {
        repository.signOut(new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                navigateToLogin.setValue(true);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    private boolean isValidPhone(String phone) {
        return phone.isEmpty() || Pattern.matches("^\\+?\\d{9,15}$", phone);
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8 &&
                Pattern.compile("[A-Z]").matcher(password).find() &&
                Pattern.compile("[a-z]").matcher(password).find() &&
                Pattern.compile("[0-9]").matcher(password).find();
    }

    public boolean esMatriculaEspanolaValida(String matricula) {
        return matricula != null && matricula.matches("^[0-9]{4}[B-DF-HJ-NP-TV-Z]{3}$");
    }

    // Getters para LiveData
    public LiveData<User> getUserData() {
        return userData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getNavigateToLogin() {
        return navigateToLogin;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<Boolean> getPasswordChangeSuccess() {
        return passwordChangeSuccess;
    }

    public LiveData<List<String>> getMatriculasList() {
        return matriculasList;
    }
}