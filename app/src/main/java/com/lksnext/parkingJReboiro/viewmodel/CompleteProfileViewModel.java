package com.lksnext.parkingJReboiro.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CompleteProfileViewModel extends ViewModel {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> profileSaved = new MutableLiveData<>();

    public CompleteProfileViewModel() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void saveUserProfile(String username, String phone, String employeeId) {
        if (username.isEmpty() || employeeId.isEmpty()) {
            errorMessage.setValue("El nombre y el ID de empleado son obligatorios");
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            errorMessage.setValue("No hay usuario autenticado");
            return;
        }

        isLoading.setValue(true);

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", user.getEmail());
        userData.put("phone", phone);
        userData.put("employeeId", employeeId);

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    profileSaved.setValue(true);
                    isLoading.setValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Error al guardar perfil: " + e.getMessage());
                    isLoading.setValue(false);
                });
    }

    public LiveData<Boolean> getProfileSaved() {
        return profileSaved;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }
}