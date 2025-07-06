package com.lksnext.parkingJReboiro.data;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseUser;
import com.lksnext.parkingJReboiro.domain.Callback;
import com.lksnext.parkingJReboiro.domain.Plaza;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.domain.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IDataRepository {
    IFirebaseUser getCurrentUser();
    void updateUserProfile(User user, Callback<Void> callback);
    LiveData<User> getUserProfile(String uid);
    void getReservasUsuarioActual(Callback<List<Reserva>> callback);
    Map<String, List<Reserva>> clasificarReservas(List<Reserva> reservas);
    void cancelarReserva(String reservaId, Callback<Void> callback);
    void loginWithEmailAndPassword(String email, String password, Callback<Void> callback);
    void loginWithGoogle(String idToken, Callback<Boolean> callback);
    void checkUserProfileComplete(Callback<Boolean> callback);
    void signOut(Callback<Void> callback);
    void getPlazasOcupadasPorPlanta(String fecha, long inicioMs, long finMs, int planta, Callback<Set<Long>> callback);
    void buscarPlazaDisponible(String fecha, long inicioMs, long finMs, String tipoPlaza, Callback<Plaza> callback);
    void verificarYGuardarReserva(Reserva reserva, boolean guardarMatricula, Callback<String> callback);
    void changePassword(String currentPassword, String newPassword, Callback<Void> callback);
    void addMatricula(String matricula, Callback<List<String>> callback);
    void removeMatricula(String matricula, Callback<List<String>> callback);
    void registerUser(String username, String email, String employeeId, String phone, String password, Callback<Void> callback);
}