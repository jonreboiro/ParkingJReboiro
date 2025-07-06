package com.lksnext.parkingJReboiro.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingJReboiro.data.DataRepository;
import com.lksnext.parkingJReboiro.data.IDataRepository;
import com.lksnext.parkingJReboiro.domain.Callback;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.domain.ReservaConTiempo;

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
import android.os.CountDownTimer;

public class DashboardViewModel extends ViewModel {

    private final IDataRepository dataRepository;
    private final MutableLiveData<List<ReservaConTiempo>> reservasActivas = new MutableLiveData<>();
    private final Map<String, CountDownTimer> timers = new HashMap<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public DashboardViewModel() {
        this.dataRepository = DataRepository.getInstance();
        cargarReservasActivas();
    }

    public DashboardViewModel(IDataRepository repository) {
        this.dataRepository = repository;
        cargarReservasActivas();
    }

    public LiveData<List<ReservaConTiempo>> getReservasActivas() {
        return reservasActivas;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public void cargarReservasActivas() {
        isLoading.setValue(true);

        dataRepository.getReservasUsuarioActual(new Callback<List<Reserva>>() {
            @Override
            public void onSuccess(List<Reserva> reservas) {
                Map<String, List<Reserva>> reservasClasificadas = dataRepository.clasificarReservas(reservas);

                // Procesar reservas actuales
                List<Reserva> actuales = reservasClasificadas.get("actual");
                if (actuales != null && !actuales.isEmpty()) {
                    List<ReservaConTiempo> reservasConTiempo = new ArrayList<>();

                    // Detener temporizadores antiguos
                    detenerTemporizadoresNoUtilizados(actuales);

                    // Inicializar con estado "En curso"
                    for (Reserva reserva : actuales) {
                        reservasConTiempo.add(new ReservaConTiempo(reserva, "En curso"));
                    }

                    // Ordenar por tiempo de finalización
                    Collections.sort(reservasConTiempo, (r1, r2) -> {
                        long tiempoFinR1 = calcularTiempoFinalizacion(r1.getReserva());
                        long tiempoFinR2 = calcularTiempoFinalizacion(r2.getReserva());
                        return Long.compare(tiempoFinR1, tiempoFinR2);
                    });

                    reservasActivas.setValue(reservasConTiempo);

                    // Calcular tiempo restante para cada reserva
                    for (int i = 0; i < actuales.size(); i++) {
                        calcularTiempoRestanteMultiple(actuales.get(i), i);
                    }
                } else {
                    // No hay reservas activas
                    reservasActivas.setValue(new ArrayList<>());
                    detenerTodosTemporizadores();
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

    public void refrescarReservasActivas() {
        cargarReservasActivas();
    }

    // El resto de métodos permanecen sin cambios ya que son métodos auxiliares
    // para manejar los temporizadores y cálculos de tiempo

    private long calcularTiempoFinalizacion(Reserva reserva) {
        // Código existente sin cambios...
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

            return calFin.getTimeInMillis();
        } catch (ParseException e) {
            return Long.MAX_VALUE; // En caso de error, mover al final
        }
    }

    private void calcularTiempoRestanteMultiple(Reserva reserva, int posicion) {
        // Código existente sin cambios...
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
                List<ReservaConTiempo> listaActual = reservasActivas.getValue();
                if (listaActual != null && posicion < listaActual.size()) {
                    listaActual.get(posicion).setTiempoRestante("Finalizada");
                    reservasActivas.setValue(new ArrayList<>(listaActual));
                }
            }
        } catch (ParseException e) {
            // Actualizar solo esta reserva en caso de error
            List<ReservaConTiempo> listaActual = reservasActivas.getValue();
            if (listaActual != null && posicion < listaActual.size()) {
                listaActual.get(posicion).setTiempoRestante("En curso");
                reservasActivas.setValue(new ArrayList<>(listaActual));
            }
        }
    }

    private void iniciarTemporizadorMultiple(long tiempoRestanteMs, String reservaId, int posicion) {
        // Código existente sin cambios...
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
                List<ReservaConTiempo> listaActual = reservasActivas.getValue();
                if (listaActual != null && posicion < listaActual.size()) {
                    listaActual.get(posicion).setTiempoRestante(tiempo);
                    reservasActivas.setValue(new ArrayList<>(listaActual));
                }
            }

            @Override
            public void onFinish() {
                // Actualizar estado cuando finaliza
                List<ReservaConTiempo> listaActual = reservasActivas.getValue();
                if (listaActual != null && posicion < listaActual.size()) {
                    listaActual.get(posicion).setTiempoRestante("Finalizada");
                    reservasActivas.setValue(new ArrayList<>(listaActual));
                }
                timers.remove(reservaId);
            }
        };

        timers.put(reservaId, timer);
        timer.start();
    }

    private void detenerTemporizadoresNoUtilizados(List<Reserva> reservasActuales) {
        // Código existente sin cambios...
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

    @Override
    protected void onCleared() {
        super.onCleared();
        detenerTodosTemporizadores();
    }
}