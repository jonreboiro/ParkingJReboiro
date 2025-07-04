package com.lksnext.parkingJReboiro.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lksnext.parkingJReboiro.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ProfileViewModel extends ViewModel {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private MutableLiveData<User> userData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> navigateToLogin = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private MutableLiveData<Boolean> passwordChangeSuccess = new MutableLiveData<>();
    private MutableLiveData<List<String>> matriculasList = new MutableLiveData<>(new ArrayList<>());



    public ProfileViewModel() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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
        DocumentReference docRef = db.collection("users").document(uid);

        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = new User();
                        user.setUsername(documentSnapshot.getString("username"));
                        user.setEmail(documentSnapshot.getString("email"));
                        user.setPhone(documentSnapshot.getString("phone"));
                        user.setEmployeeId(documentSnapshot.getString("employeeId"));

                        List<String> matriculas = (List<String>) documentSnapshot.get("matriculas");
                        if (matriculas != null) {
                            user.setMatriculas(matriculas);
                            matriculasList.setValue(matriculas);
                        }
                        userData.setValue(user);
                    }
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Error al cargar datos: " + e.getMessage());
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

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            navigateToLogin.setValue(true);
            return;
        }

        isLoading.setValue(true);
        String uid = user.getUid();

        Map<String, Object> datos = new HashMap<>();
        datos.put("username", username);
        datos.put("email", email);
        datos.put("phone", phone);
        datos.put("employeeId", employeeId);

        db.collection("users").document(uid).set(datos)
                .addOnSuccessListener(aVoid -> {
                    updateSuccess.setValue(true);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Error al guardar cambios: " + e.getMessage());
                    isLoading.setValue(false);
                });
    }

    public void changePassword(String newPassword, String currentPassword) {
        if (!isPasswordValid(newPassword)) {
            errorMessage.setValue("La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            errorMessage.setValue("Necesitas volver a iniciar sesión para cambiar la contraseña");
            navigateToLogin.setValue(true);
            return;
        }

        isLoading.setValue(true);

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(task1 -> {
                                    isLoading.setValue(false);
                                    if (task1.isSuccessful()) {
                                        passwordChangeSuccess.setValue(true);
                                    } else {
                                        errorMessage.setValue("Error al cambiar la contraseña");
                                        passwordChangeSuccess.setValue(false);
                                    }
                                });
                    } else {
                        isLoading.setValue(false);
                        errorMessage.setValue("Contraseña actual incorrecta");
                    }
                });
    }

    public void addPlate(String matricula) {
        if (!esMatriculaEspanolaValida(matricula)) {
            errorMessage.setValue("Formato de matrícula inválido. Debe ser 4 números seguidos de 3 consonantes mayúsculas.");
            return;
        }

        List<String> currentList = matriculasList.getValue();
        if (currentList != null && currentList.contains(matricula)) {
            errorMessage.setValue("Esta matrícula ya está registrada.");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            navigateToLogin.setValue(true);
            return;
        }

        isLoading.setValue(true);
        String uid = user.getUid();

        List<String> updatedList = new ArrayList<>();
        if (currentList != null) {
            updatedList.addAll(currentList);
        }
        updatedList.add(matricula);

        db.collection("users").document(uid)
                .update("matriculas", updatedList)
                .addOnSuccessListener(aVoid -> {
                    matriculasList.setValue(updatedList);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Error al guardar la matrícula: " + e.getMessage());
                    isLoading.setValue(false);
                });
    }

    public void removePlate(String matricula) {
        List<String> currentList = matriculasList.getValue();
        if (currentList == null || !currentList.contains(matricula)) {
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            navigateToLogin.setValue(true);
            return;
        }

        isLoading.setValue(true);
        String uid = user.getUid();

        List<String> updatedList = new ArrayList<>(currentList);
        updatedList.remove(matricula);

        db.collection("users").document(uid)
                .update("matriculas", updatedList)
                .addOnSuccessListener(aVoid -> {
                    matriculasList.setValue(updatedList);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Error al eliminar la matrícula: " + e.getMessage());
                    isLoading.setValue(false);
                });
    }


    public void logout() {
        mAuth.signOut();
        navigateToLogin.setValue(true);
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