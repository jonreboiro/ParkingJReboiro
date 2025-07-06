package com.lksnext.parkingJReboiro.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lksnext.parkingJReboiro.domain.Callback;
import com.lksnext.parkingJReboiro.domain.Plaza;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Obtiene las plazas ocupadas para una fecha y horario específicos
     */
    public void getPlazasOcupadasPorPlanta(String fecha, long inicioMs, long finMs, int planta,
                                           Callback<Set<Long>> callback) {
        db.collection("reservas")
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnSuccessListener(reservasSnap -> {
                    Set<Long> plazasOcup = new HashSet<>();

                    for (DocumentSnapshot doc : reservasSnap.getDocuments()) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        if (reserva != null) {
                            long plazaId = reserva.getPlazaId().getId();

                            // Filtrar por planta
                            boolean esDePlanta = (planta == 0 && plazaId >= 13 && plazaId <= 27)
                                    || (planta == -1 && plazaId >= 1 && plazaId <= 12);

                            if (esDePlanta) {
                                long reservaInicio = reserva.getHoraInicio().getHoraInicio();
                                long reservaFin = reserva.getHoraInicio().getHoraFin();

                                if (inicioMs < reservaFin && reservaInicio < finMs) {
                                    plazasOcup.add(plazaId);
                                }
                            }
                        }
                    }

                    callback.onSuccess(plazasOcup);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Busca una plaza disponible de un tipo específico
     */
    public void buscarPlazaDisponible(String fecha, long inicioMs, long finMs, String tipoPlaza,
                                      Callback<Plaza> callback) {
        db.collection("reservas")
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnSuccessListener(reservasSnap -> {
                    Set<Long> ocupadas = new HashSet<>();

                    for (DocumentSnapshot doc : reservasSnap.getDocuments()) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        if (reserva != null && tipoPlaza.equals(reserva.getPlazaId().getTipo())) {
                            long reservaInicio = reserva.getHoraInicio().getHoraInicio();
                            long reservaFin = reserva.getHoraInicio().getHoraFin();
                            if (inicioMs < reservaFin && reservaInicio < finMs) {
                                ocupadas.add(reserva.getPlazaId().getId());
                            }
                        }
                    }

                    List<Plaza> plazasTipo = obtenerPlazasPorTipo(tipoPlaza);
                    Plaza libre = null;
                    for (Plaza p : plazasTipo) {
                        if (!ocupadas.contains(p.getId())) {
                            libre = p;
                            break;
                        }
                    }

                    callback.onSuccess(libre);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Obtiene todas las plazas de un tipo específico
     */
    private List<Plaza> obtenerPlazasPorTipo(String tipo) {
        List<Plaza> plazas = new ArrayList<>();
        switch (tipo) {
            case "normal":
                long[] normales = {2, 3, 4, 5, 8, 9, 10, 11, 18, 19, 20, 21, 23, 24, 25, 26, 27};
                for (long id : normales) plazas.add(new Plaza(id, "normal"));
                break;
            case "moto":
                long[] motos = {13, 14, 15, 16, 17};
                for (long id : motos) plazas.add(new Plaza(id, "moto"));
                break;
            case "electrico":
                long[] electricos = {6, 12};
                for (long id : electricos) plazas.add(new Plaza(id, "electrico"));
                break;
            case "minusvalido":
                long[] minusvalidos = {1, 7, 22};
                for (long id : minusvalidos) plazas.add(new Plaza(id, "minusvalido"));
                break;
        }
        return plazas;
    }

    /**
     * Verifica y guarda una reserva
     */
    public void verificarYGuardarReserva(Reserva reserva, boolean guardarMatricula, Callback<String> callback) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onError("No hay usuario autenticado");
            return;
        }

        reserva.setUserId(currentUser.getUid());
        long inicioMs = reserva.getHoraInicio().getHoraInicio();
        long finMs = reserva.getHoraInicio().getHoraFin();

        // Verificar si la plaza está disponible
        db.collection("reservas")
                .whereEqualTo("fecha", reserva.getFecha())
                .get()
                .addOnSuccessListener(reservasSnap -> {
                    boolean plazaOcupada = false;

                    for (DocumentSnapshot doc : reservasSnap.getDocuments()) {
                        Reserva reservaExistente = doc.toObject(Reserva.class);
                        if (reservaExistente != null &&
                                reservaExistente.getPlazaId().getId() == reserva.getPlazaId().getId()) {
                            long reservaInicio = reservaExistente.getHoraInicio().getHoraInicio();
                            long reservaFin = reservaExistente.getHoraInicio().getHoraFin();

                            if (inicioMs < reservaFin && reservaInicio < finMs) {
                                plazaOcupada = true;
                                break;
                            }
                        }
                    }

                    if (plazaOcupada) {
                        callback.onError("La plaza ya ha sido reservada");
                    } else {
                        guardarNuevaReserva(reserva, guardarMatricula, callback);
                    }
                })
                .addOnFailureListener(e -> callback.onError("Error al verificar disponibilidad: " + e.getMessage()));
    }

    /**
     * Guarda la reserva en Firestore
     */
    private void guardarNuevaReserva(Reserva reserva, boolean guardarMatricula, Callback<String> callback) {
        db.collection("reservas")
                .add(reserva)
                .addOnSuccessListener(documentReference -> {
                    String id = documentReference.getId();
                    if (guardarMatricula) {
                        guardarMatriculaEnPerfil(reserva.getMatricula());
                    }
                    callback.onSuccess(id);
                })
                .addOnFailureListener(e -> callback.onError("Error al guardar la reserva: " + e.getMessage()));
    }

    /**
     * Guarda la matrícula en el perfil del usuario
     */
    private void guardarMatriculaEnPerfil(String matricula) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> matriculas = new ArrayList<>();

                    if (documentSnapshot.exists() && documentSnapshot.contains("matriculas")) {
                        List<String> matriculasExistentes = (List<String>) documentSnapshot.get("matriculas");
                        if (matriculasExistentes != null) {
                            matriculas.addAll(matriculasExistentes);
                        }
                    }

                    if (!matriculas.contains(matricula)) {
                        matriculas.add(matricula);
                        db.collection("users").document(userId)
                                .update("matriculas", matriculas)
                                .addOnFailureListener(e ->
                                        System.out.println("Error al guardar matrícula: " + e.getMessage()));
                    }
                });
    }

    public void changePassword(String currentPassword, String newPassword, Callback<Void> callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onError("Necesitas volver a iniciar sesión para cambiar la contraseña");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        callback.onSuccess(null);
                                    } else {
                                        callback.onError("Error al cambiar la contraseña");
                                    }
                                });
                    } else {
                        callback.onError("Contraseña actual incorrecta");
                    }
                });
    }

    public void addMatricula(String matricula, Callback<List<String>> callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onError("No hay usuario autenticado");
            return;
        }

        String uid = user.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> matriculas = new ArrayList<>();

                    if (documentSnapshot.exists() && documentSnapshot.contains("matriculas")) {
                        List<String> existingMatriculas = (List<String>) documentSnapshot.get("matriculas");
                        if (existingMatriculas != null) {
                            matriculas.addAll(existingMatriculas);
                        }
                    }

                    if (matriculas.contains(matricula)) {
                        callback.onError("Esta matrícula ya está registrada.");
                        return;
                    }

                    matriculas.add(matricula);

                    db.collection("users").document(uid)
                            .update("matriculas", matriculas)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(matriculas))
                            .addOnFailureListener(e -> callback.onError("Error al guardar la matrícula: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Error al obtener datos: " + e.getMessage()));
    }

    public void removeMatricula(String matricula, Callback<List<String>> callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onError("No hay usuario autenticado");
            return;
        }

        String uid = user.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || !documentSnapshot.contains("matriculas")) {
                        callback.onError("No se encontraron matrículas registradas");
                        return;
                    }

                    List<String> matriculas = (List<String>) documentSnapshot.get("matriculas");
                    if (matriculas == null || !matriculas.contains(matricula)) {
                        callback.onError("La matrícula no está registrada");
                        return;
                    }

                    matriculas.remove(matricula);

                    db.collection("users").document(uid)
                            .update("matriculas", matriculas)
                            .addOnSuccessListener(aVoid -> callback.onSuccess(matriculas))
                            .addOnFailureListener(e -> callback.onError("Error al eliminar la matrícula: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Error al obtener datos: " + e.getMessage()));
    }
}