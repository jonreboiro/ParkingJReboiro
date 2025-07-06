package com.lksnext.parkingJReboiro.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.lksnext.parkingJReboiro.data.DataRepository;
import com.lksnext.parkingJReboiro.data.IDataRepository;
import com.lksnext.parkingJReboiro.data.IFirebaseUser;
import com.lksnext.parkingJReboiro.domain.Callback;
import com.lksnext.parkingJReboiro.domain.User;

public class CompleteProfileViewModel extends ViewModel {

    private final IDataRepository repository;

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> profileSaved = new MutableLiveData<>();

    public CompleteProfileViewModel() {
        this(DataRepository.getInstance());
    }

    // Constructor existente para pruebas
    public CompleteProfileViewModel(IDataRepository repository) {
        this.repository = repository;
    }
    public void saveUserProfile(String username, String phone, String employeeId) {
        if (username.isEmpty() || employeeId.isEmpty()) {
            errorMessage.setValue("El nombre y el ID de empleado son obligatorios");
            return;
        }

        IFirebaseUser currentUser = repository.getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("No hay usuario autenticado");
            return;
        }

        isLoading.setValue(true);

        User user = new User();
        user.setUsername(username);
        user.setEmail(currentUser.getEmail());
        user.setPhone(phone);
        user.setEmployeeId(employeeId);

        repository.updateUserProfile(user, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                profileSaved.setValue(true);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
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