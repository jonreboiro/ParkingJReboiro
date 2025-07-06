package com.lksnext.parkingJReboiro.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lksnext.parkingJReboiro.domain.Callback;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.domain.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataRepository {

    private static DataRepository instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final ReservationManager reservationManager;

    private DataRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        reservationManager = new ReservationManager();
    }

    // Creación de la instancia en caso de que no exista.
    public static synchronized DataRepository getInstance() {
        if (instance == null) {
            instance = new DataRepository();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public LiveData<User> getUserProfile(String uid) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        userLiveData.setValue(user);
                    } else {
                        userLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> userLiveData.setValue(null));

        return userLiveData;
    }

    public void updateUserProfile(User user, Callback callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("No hay usuario autenticado");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("phone", user.getPhone());
        userData.put("employeeId", user.getEmployeeId());

        db.collection("users").document(currentUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError("Error al guardar perfil: " + e.getMessage()));
    }

    /**
     * Obtiene las reservas activas del usuario actual
     */
    public void getReservasUsuarioActual(final Callback<List<Reserva>> callback) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            callback.onError("No hay usuario autenticado");
            return;
        }

        String userId = currentUser.getUid();
        reservationManager.getReservasDelUsuario(userId, new ReservationManager.ReservasCallback() {
            @Override
            public void onReservasObtenidas(List<Reserva> reservas) {
                callback.onSuccess(reservas);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Clasifica las reservas en actuales, próximas y pasadas
     */
    public Map<String, List<Reserva>> clasificarReservas(List<Reserva> reservas) {
        return reservationManager.clasificarReservas(reservas);
    }

    /**
     * Cancela una reserva
     */
    public void cancelarReserva(String reservaId, Callback<Void> callback) {
        reservationManager.cancelarReserva(
                reservaId,
                unused -> callback.onSuccess(null),
                e -> callback.onError(e.getMessage()));
    }

    public void loginWithEmailAndPassword(String email, String password, Callback<Void> callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(obtenerMensajeError(e)));
    }

    public void loginWithGoogle(String idToken, Callback<Boolean> callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> checkUserProfileComplete(callback))
                .addOnFailureListener(e -> callback.onError("Error autenticando con Google"));
    }

    public void checkUserProfileComplete(Callback<Boolean> callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        boolean needsCompletion = !documentSnapshot.exists() ||
                                !documentSnapshot.contains("employeeId") ||
                                documentSnapshot.getString("employeeId") == null;
                        callback.onSuccess(needsCompletion);
                    })
                    .addOnFailureListener(e -> callback.onError("Error al verificar perfil"));
        } else {
            callback.onError("Error de autenticación");
        }
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

    public void signOut(Callback<Void> callback) {
        try {
            mAuth.signOut();
            callback.onSuccess(null);
        } catch (Exception e) {
            callback.onError("Error al cerrar sesión: " + e.getMessage());
        }
    }
}