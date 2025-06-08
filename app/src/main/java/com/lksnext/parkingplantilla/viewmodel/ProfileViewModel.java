// app/src/main/java/com/lksnext/parkingplantilla/viewmodel/ProfileViewModel.java
package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Callback;
import com.lksnext.parkingplantilla.domain.User;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<User> userData = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    public ProfileViewModel() {
        // Cargar datos del usuario desde el repositorio
        loadUserData();
    }

    private void loadUserData() {
        // Aquí cargaríamos los datos del usuario actual desde el repositorio
        // Por ahora, creamos datos de ejemplo
        User user = new User();
        user.setUsername("Usuario ejemplo");
        user.setEmail("usuario@ejemplo.com");
        user.setPhone("666555444");
        user.setEmployeeId("EMP12345");
        userData.setValue(user);
    }

    public LiveData<User> getUserData() {
        return userData;
    }

    public LiveData<Boolean> getUpdateResult() {
        return updateSuccess;
    }

    public void updateUserProfile(String username, String email, String phone, String employeeId) {
        // Aquí actualizaríamos los datos en el repositorio
        // Por ahora, solo actualizamos en el LiveData
        User currentUser = userData.getValue();
        if (currentUser != null) {
            currentUser.setUsername(username);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            currentUser.setEmployeeId(employeeId);
            userData.setValue(currentUser);
            updateSuccess.setValue(true);
        }
    }

    public void logout() {
        // Eliminar datos de sesión
        DataRepository.getInstance().logout();
    }
}