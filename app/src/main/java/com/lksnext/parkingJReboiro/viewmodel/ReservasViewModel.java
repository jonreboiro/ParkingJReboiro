package com.lksnext.parkingJReboiro.viewmodel;

import android.content.Context;
import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingJReboiro.data.DataRepository;
import com.lksnext.parkingJReboiro.domain.Callback;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.domain.ReservaConTiempo;
import com.lksnext.parkingJReboiro.notifications.NotificationScheduler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class ReservasViewModel extends ViewModel {

    private final DataRepository dataRepository;
    private final MutableLiveData<List<Reserva>> reservasProximas = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Reserva>> reservasPasadas = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ReservaConTiempo>> reservasActivasConTiempo = new MutableLiveData<>(new ArrayList<>());
    private final Map<String, CountDownTimer> timers = new HashMap<>();

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> tiempoRestante = new MutableLiveData<>("En curso");
    private final MutableLiveData<Boolean> operacionExitosa = new MutableLiveData<>();

    private CountDownTimer countDownTimer;
    private boolean timerRunning = false;

    public ReservasViewModel() {
        this.dataRepository = DataRepository.getInstance();
    }

    // Getters para LiveData (sin cambios)
    public LiveData<List<Reserva>> getReservasProximas() {
        return reservasProximas;
    }

    public LiveData<List<Reserva>> getReservasPasadas() {
        return reservasPasadas;
    }

    public LiveData<List<ReservaConTiempo>> getReservasActivasConTiempo() {
        return reservasActivasConTiempo;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getTiempoRestante() {
        return tiempoRestante;
    }

    public LiveData<Boolean> getOperacionExitosa() {
        return operacionExitosa;
    }

    public void cargarReservasUsuario() {
        isLoading.setValue(true);

        if (dataRepository.getCurrentUser() == null) {
            errorMessage.setValue("Debes iniciar sesión para ver tus reservas");
            isLoading.setValue(false);
            return;
        }

        dataRepository.getReservasUsuarioActual(new Callback<List<Reserva>>() {
            @Override
            public void onSuccess(List<Reserva> reservas) {
                Map<String, List<Reserva>> reservasClasificadas = dataRepository.clasificarReservas(reservas);

                // Procesar reserva actual
                List<Reserva> actuales = reservasClasificadas.get("actual");
                if (actuales != null && !actuales.isEmpty()) {
                    List<ReservaConTiempo> reservasConTiempo = new ArrayList<>();
                    detenerTemporizadoresNoUtilizados(actuales);

                    for (Reserva reserva : actuales) {
                        reservasConTiempo.add(new ReservaConTiempo(reserva, "En curso"));
                    }

                    Collections.sort(reservasConTiempo, (r1, r2) -> {
                        long tiempoFinR1 = calcularTiempoFinalizacion(r1.getReserva());
                        long tiempoFinR2 = calcularTiempoFinalizacion(r2.getReserva());
                        return Long.compare(tiempoFinR1, tiempoFinR2);
                    });

                    reservasActivasConTiempo.setValue(reservasConTiempo);

                    for (int i = 0; i < actuales.size(); i++) {
                        calcularTiempoRestanteMultiple(actuales.get(i), i);
                    }
                } else {
                    reservasActivasConTiempo.setValue(new ArrayList<>());
                    detenerTodosTemporizadores();
                }

                // Procesar reservas próximas
                List<Reserva> proximas = reservasClasificadas.get("proximas");
                if (proximas != null && !proximas.isEmpty()) {
                    Collections.sort(proximas, (r1, r2) -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        try {
                            Date fecha1 = sdf.parse(r1.getFecha());
                            Date fecha2 = sdf.parse(r2.getFecha());
                            int comparacionFecha = fecha1.compareTo(fecha2);

                            if (comparacionFecha != 0) {
                                return comparacionFecha;
                            } else {
                                return Long.compare(r1.getHoraInicio().getHoraInicio(),
                                        r2.getHoraInicio().getHoraInicio());
                            }
                        } catch (ParseException e) {
                            return 0;
                        }
                    });
                }
                reservasProximas.setValue(proximas != null ? proximas : new ArrayList<>());

                // Procesar reservas pasadas
                List<Reserva> pasadas = reservasClasificadas.get("pasadas");
                if (pasadas != null) {
                    Collections.sort(pasadas, (r1, r2) -> {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        try {
                            Date fecha1 = sdf.parse(r1.getFecha());
                            Date fecha2 = sdf.parse(r2.getFecha());
                            return fecha2.compareTo(fecha1); // Orden descendente
                        } catch (ParseException e) {
                            return 0;
                        }
                    });
                    reservasPasadas.setValue(pasadas);
                } else {
                    reservasPasadas.setValue(new ArrayList<>());
                }

                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue("Error al cargar reservas: " + message);
                isLoading.setValue(false);
            }
        });
    }

    public void cancelarReserva(Reserva reserva, int position, Context context) {
        isLoading.setValue(true);
        dataRepository.cancelarReserva(reserva.getId(), new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<Reserva> listaActual = reservasProximas.getValue();
                if (listaActual != null) {
                    listaActual.remove(position);
                    reservasProximas.setValue(listaActual);
                }
                NotificationScheduler.showInstantNotification(
                        context,
                        101,
                        "Reserva cancelada",
                        "Tu reserva ha sido cancelada correctamente."
                );
                operacionExitosa.setValue(true);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue("Error al cancelar la reserva: " + message);
                isLoading.setValue(false);
            }
        });
    }

    private long calcularTiempoFinalizacion(Reserva reserva) {
        try {
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fechaReserva = formatoFecha.parse(reserva.getFecha());

            Calendar calFin = Calendar.getInstance();
            calFin.setTime(fechaReserva);

            long horaFinMs = reserva.getHoraInicio().getHoraFin();
            int horaFinInt = (int)(horaFinMs / (60 * 60 * 1000));
            int minFinInt = (int)((horaFinMs % (60 * 60 * 1000)) / (60 * 1000));

            calFin.set(Calendar.HOUR_OF_DAY, horaFinInt);
            calFin.set(Calendar.MINUTE, minFinInt);
            calFin.set(Calendar.SECOND, 0);
            calFin.set(Calendar.MILLISECOND, 0);

            return calFin.getTimeInMillis();
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }

    private void calcularTiempoRestanteMultiple(Reserva reserva, int posicion) {
        try {
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fechaReserva = formatoFecha.parse(reserva.getFecha());

            Calendar calFin = Calendar.getInstance();
            calFin.setTime(fechaReserva);

            // Configurar hora de finalización
            long horaFinMs = reserva.getHoraInicio().getHoraFin();
            int horaFinInt = (int)(horaFinMs / (60 * 60 * 1000));
            int minFinInt = (int)((horaFinMs % (60 * 60 * 1000)) / (60 * 1000));

            calFin.set(Calendar.HOUR_OF_DAY, horaFinInt);
            calFin.set(Calendar.MINUTE, minFinInt);
            calFin.set(Calendar.SECOND, 0);
            calFin.set(Calendar.MILLISECOND, 0);

            long tiempoFinReal = calFin.getTimeInMillis();
            long tiempoActualMs = System.currentTimeMillis();
            long tiempoRestanteMs = tiempoFinReal - tiempoActualMs;

            if (tiempoRestanteMs > 0) {
                iniciarTemporizadorMultiple(tiempoRestanteMs, reserva.getId(), posicion);
            } else {
                // Actualizar solo esta reserva en la lista
                List<ReservaConTiempo> listaActual = reservasActivasConTiempo.getValue();
                if (listaActual != null && posicion < listaActual.size()) {
                    listaActual.get(posicion).setTiempoRestante("Finalizada");
                    reservasActivasConTiempo.setValue(new ArrayList<>(listaActual));
                }
            }
        } catch (ParseException e) {
            // Actualizar solo esta reserva en la lista
            List<ReservaConTiempo> listaActual = reservasActivasConTiempo.getValue();
            if (listaActual != null && posicion < listaActual.size()) {
                listaActual.get(posicion).setTiempoRestante("En curso");
                reservasActivasConTiempo.setValue(new ArrayList<>(listaActual));
            }
        }
    }

    private void iniciarTemporizadorMultiple(long tiempoRestanteMs, String reservaId, int posicion) {
        // Detener el temporizador anterior para esta reserva si existe
        if (timers.containsKey(reservaId)) {
            timers.get(reservaId).cancel();
        }

        // Crear un nuevo temporizador
        CountDownTimer timer = new CountDownTimer(tiempoRestanteMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long horas = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                long minutos = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;

                String tiempo = String.format("En curso - %02d:%02d", horas, minutos);

                // Actualizar solo esta reserva en la lista
                List<ReservaConTiempo> listaActual = reservasActivasConTiempo.getValue();
                if (listaActual != null && posicion < listaActual.size()) {
                    listaActual.get(posicion).setTiempoRestante(tiempo);
                    reservasActivasConTiempo.setValue(new ArrayList<>(listaActual));
                }
            }

            @Override
            public void onFinish() {
                // Actualizar estado cuando finaliza
                List<ReservaConTiempo> listaActual = reservasActivasConTiempo.getValue();
                if (listaActual != null && posicion < listaActual.size()) {
                    listaActual.get(posicion).setTiempoRestante("Finalizada");
                    reservasActivasConTiempo.setValue(new ArrayList<>(listaActual));
                }
                timers.remove(reservaId);
            }
        };

        timers.put(reservaId, timer);
        timer.start();
    }

    private void detenerTemporizadoresNoUtilizados(List<Reserva> reservasActuales) {
        Set<String> idsActuales = new HashSet<>();
        for (Reserva reserva : reservasActuales) {
            idsActuales.add(reserva.getId());
        }

        Set<String> idsParaEliminar = new HashSet<>();
        for (Map.Entry<String, CountDownTimer> entry : timers.entrySet()) {
            if (!idsActuales.contains(entry.getKey())) {
                entry.getValue().cancel();
                idsParaEliminar.add(entry.getKey());
            }
        }

        for (String id : idsParaEliminar) {
            timers.remove(id);
        }
    }

    private void detenerTodosTemporizadores() {
        for (CountDownTimer timer : timers.values()) {
            timer.cancel();
        }
        timers.clear();
    }

    public void resetOperacionExitosa() {
        operacionExitosa.setValue(null);
    }

    public void detenerTemporizador() {
        if (countDownTimer != null && timerRunning) {
            countDownTimer.cancel();
            timerRunning = false;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        detenerTodosTemporizadores();
    }
}