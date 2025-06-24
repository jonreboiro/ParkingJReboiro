package com.lksnext.parkingJReboiro.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lksnext.parkingJReboiro.domain.Reserva;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReservationManager {

    /**
     * Interfaz para manejar las respuestas de Firebase
     */
    public interface ReservasCallback {
        void onReservasObtenidas(List<Reserva> reservas);
        void onError(Exception e);
    }

    /**
     * Obtiene todas las reservas del usuario especificado
     * @param userId ID del usuario actual
     * @param callback Callback para manejar la respuesta
     */
    public void getReservasDelUsuario(String userId, ReservasCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservas")
                .whereEqualTo("usuario", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Reserva> reservas = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Reserva reserva = document.toObject(Reserva.class);
                        reserva.setId(document.getId());
                        reservas.add(reserva);
                    }
                    callback.onReservasObtenidas(reservas);
                })
                .addOnFailureListener(callback::onError);
    }

    /**
     * Clasifica las reservas en actual (en curso), próximas y pasadas
     * @param reservas Lista de reservas a clasificar
     * @return Map con las reservas clasificadas por categoría
     */
    public Map<String, List<Reserva>> clasificarReservas(List<Reserva> reservas) {
        Map<String, List<Reserva>> reservasClasificadas = new HashMap<>();

        // Inicializar las listas para cada categoría
        List<Reserva> reservaActual = new ArrayList<>();
        List<Reserva> reservasProximas = new ArrayList<>();
        List<Reserva> reservasPasadas = new ArrayList<>();

        // Obtener la fecha actual
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaActual = formatoFecha.format(new Date());

        // Obtener la hora actual en milisegundos desde medianoche
        Calendar cal = Calendar.getInstance();
        long msSinceMedianoche =
                cal.get(Calendar.HOUR_OF_DAY) * 3600000L +
                        cal.get(Calendar.MINUTE) * 60000L +
                        cal.get(Calendar.SECOND) * 1000L;

        for (Reserva reserva : reservas) {
            String fechaReserva = reserva.getFecha();
            long horaInicio = reserva.getHoraInicio().getHoraInicio();
            long horaFin = reserva.getHoraInicio().getHoraFin();

            int comparacionFechas;
            try {
                Date fechaReservaDate = formatoFecha.parse(fechaReserva);
                Date fechaActualDate = formatoFecha.parse(fechaActual);
                comparacionFechas = fechaReservaDate.compareTo(fechaActualDate);
            } catch (ParseException e) {
                // Si hay error en el formato, comparamos las cadenas directamente
                comparacionFechas = fechaReserva.compareTo(fechaActual);
            }

            if (comparacionFechas < 0) {
                // Reserva en fecha pasada
                reservasPasadas.add(reserva);
            } else if (comparacionFechas > 0) {
                // Reserva en fecha futura
                reservasProximas.add(reserva);
            } else {
                // Reserva en la fecha actual
                if (msSinceMedianoche >= horaInicio && msSinceMedianoche < horaFin) {
                    reservaActual.add(reserva);
                } else if (msSinceMedianoche < horaInicio) {
                    reservasProximas.add(reserva);
                } else {
                    reservasPasadas.add(reserva);
                }
            }
        }

        // Guardar las listas en el mapa
        reservasClasificadas.put("actual", reservaActual);
        reservasClasificadas.put("proximas", reservasProximas);
        reservasClasificadas.put("pasadas", reservasPasadas);

        return reservasClasificadas;
    }

    /**
     * Cancela una reserva específica
     * @param reservaId ID de la reserva a cancelar
     * @param onSuccess Listener para manejar éxito
     * @param onFailure Listener para manejar error
     */
    public void cancelarReserva(String reservaId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservas")
                .document(reservaId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
}