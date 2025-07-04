package com.lksnext.parkingJReboiro.viewmodel;

import android.content.Context;
import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lksnext.parkingJReboiro.domain.Hora;
import com.lksnext.parkingJReboiro.domain.Plaza;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.notifications.NotificationScheduler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NuevaReservaViewModel extends ViewModel {
    private Integer selectedYear, selectedMonth, selectedDay;

    private final MutableLiveData<String> tiempoRestante = new MutableLiveData<>("En curso");
    private CountDownTimer countDownTimer;
    private boolean timerRunning = false;

    // LiveData para datos de reserva
    private final MutableLiveData<String> fecha = new MutableLiveData<>();
    private final MutableLiveData<Integer> horaInicio = new MutableLiveData<>();
    private final MutableLiveData<Integer> minutosInicio = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> duracion = new MutableLiveData<>(1);

    // Plaza seleccionada
    private final MutableLiveData<Long> plazaId = new MutableLiveData<>();
    private final MutableLiveData<String> tipoPlaza = new MutableLiveData<>();

    // Plazas ocupadas
    private final MutableLiveData<Set<Long>> plazasOcupadas = new MutableLiveData<>(new HashSet<>());

    // Estados y mensajes
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> reservaExitosa = new MutableLiveData<>(false);

    private final MutableLiveData<Plaza> plazaDisponible = new MutableLiveData<>();
    private final MutableLiveData<Boolean> busquedaIntentada = new MutableLiveData<>(false);
    private final MutableLiveData<String> plazaTipoSeleccionada = new MutableLiveData<>("normal");
    public LiveData<String> getPlazaTipoSeleccionada() { return plazaTipoSeleccionada; }
    private final MutableLiveData<String> matricula = new MutableLiveData<>();
    private final MutableLiveData<Boolean> guardarMatricula = new MutableLiveData<>(false);

    public void setPlazaTipoSeleccionada(String tipo) { plazaTipoSeleccionada.setValue(tipo); }

    public LiveData<Plaza> getPlazaDisponible() { return plazaDisponible; }
    public LiveData<Boolean> getBusquedaIntentada() { return busquedaIntentada; }

    public Integer getSelectedYear() { return selectedYear; }
    public Integer getSelectedMonth() { return selectedMonth; }
    public Integer getSelectedDay() { return selectedDay; }
    // Getters para LiveData
    public LiveData<String> getFecha() { return fecha; }
    public LiveData<Integer> getHoraInicio() { return horaInicio; }
    public LiveData<Integer> getMinutosInicio() { return minutosInicio; }
    public LiveData<Integer> getDuracion() { return duracion; }
    public LiveData<Long> getPlazaId() { return plazaId; }
    public LiveData<String> getTipoPlaza() { return tipoPlaza; }
    public LiveData<Set<Long>> getPlazasOcupadas() { return plazasOcupadas; }
    public LiveData<String> getMensajeError() { return mensajeError; }
    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<Boolean> getReservaExitosa() { return reservaExitosa; }

    // Setters
    public void setFecha(String fecha) { this.fecha.setValue(fecha); }
    public void setHoraInicio(int hora) { this.horaInicio.setValue(hora); }
    public void setMinutosInicio(int minutos) { this.minutosInicio.setValue(minutos); }
    public void setDuracion(int duracion) { this.duracion.setValue(duracion); }
    public LiveData<String> getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula.setValue(matricula); }

    public LiveData<Boolean> getGuardarMatricula() { return guardarMatricula; }
    public void setGuardarMatricula(boolean guardar) { this.guardarMatricula.setValue(guardar); }

    public void seleccionarPlaza(long id, String tipo) {
        this.plazaId.setValue(id);
        this.tipoPlaza.setValue(tipo);
    }

    public LiveData<String> getTiempoRestante() {
        return tiempoRestante;
    }

    private void iniciarTemporizador(long tiempoRestanteMs) {
        detenerTemporizador();

        countDownTimer = new CountDownTimer(tiempoRestanteMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long horas = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                long minutos = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                String tiempo = String.format("En curso - Tiempo restante: %02d:%02d", horas, minutos);
                tiempoRestante.setValue(tiempo);
            }

            @Override
            public void onFinish() {
                tiempoRestante.setValue("Finalizada");
                timerRunning = false;
            }
        };

        countDownTimer.start();
        timerRunning = true;
    }

    public void detenerTemporizador() {
        if (countDownTimer != null && timerRunning) {
            countDownTimer.cancel();
            timerRunning = false;
        }
    }

    /**
     * Valida la selección de fecha y hora
     */
    public boolean validarSeleccion() {
        if (selectedYear == null || selectedMonth == null || selectedDay == null ||
                horaInicio.getValue() == null) {
            mensajeError.setValue("Selecciona fecha y hora");
            return false;
        }

        Calendar seleccionada = Calendar.getInstance();
        seleccionada.set(selectedYear, selectedMonth, selectedDay,
                horaInicio.getValue(), minutosInicio.getValue(), 0);

        if (seleccionada.before(Calendar.getInstance())) {
            mensajeError.setValue("No puedes seleccionar una fecha/hora pasada");
            return false;
        }

        int duracionValue = duracion.getValue() != null ? duracion.getValue() : 0;
        if (duracionValue < 1 || duracionValue > 8) {
            mensajeError.setValue("Duración debe ser entre 1 y 8 horas");
            return false;
        }

        return true;
    }

    /**
     * Carga las plazas ocupadas para la fecha y horario seleccionados
     */
    public void cargarPlazasOcupadas(int planta) {
        if (fecha.getValue() == null || horaInicio.getValue() == null) {
            return;
        }

        cargando.setValue(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservas")
                .whereEqualTo("fecha", fecha.getValue())
                .get()
                .addOnSuccessListener(reservasSnap -> {
                    Set<Long> plazasOcup = new HashSet<>();

                    long miInicio = (horaInicio.getValue() * 60L + minutosInicio.getValue()) * 60_000L;
                    long miFin = miInicio + duracion.getValue() * 60 * 60_000L;

                    for (QueryDocumentSnapshot doc : reservasSnap) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        long plazaId = reserva.getPlazaId().getId();

                        // Filtrar por planta
                        boolean esDePlanta = (planta == 0 && plazaId >= 13 && plazaId <= 27)
                                || (planta == -1 && plazaId >= 1 && plazaId <= 12);

                        if (esDePlanta) {
                            long reservaInicio = reserva.getHoraInicio().getHoraInicio();
                            long reservaFin = reserva.getHoraInicio().getHoraFin();

                            if (miInicio < reservaFin && reservaInicio < miFin) {
                                plazasOcup.add(plazaId);
                            }
                        }
                    }

                    plazasOcupadas.setValue(plazasOcup);
                    cargando.setValue(false);
                })
                .addOnFailureListener(e -> {
                    mensajeError.setValue("Error al cargar reservas");
                    cargando.setValue(false);
                });
    }

    public boolean esMatriculaEspanolaValida(String matricula) {
        return matricula != null && matricula.matches("^[0-9]{4}[B-DF-HJ-NP-TV-Z]{3}$");
    }

    /**
     * Verifica y guarda una nueva reserva
     */
    public void verificarYGuardarReserva(Context context) {
        if (fecha.getValue() == null || horaInicio.getValue() == null ||
                plazaId.getValue() == null) {
            mensajeError.setValue("Faltan datos para realizar la reserva");
            return;
        }
        if (matricula.getValue() == null || matricula.getValue().isEmpty()) {
            mensajeError.setValue("Debes seleccionar una matrícula");
            return;
        }

        if (!esMatriculaEspanolaValida(matricula.getValue())) {
            mensajeError.setValue("El formato de la matrícula no es válido");
            return;
        }

        cargando.setValue(true);

        long inicioMs = (horaInicio.getValue() * 60L + minutosInicio.getValue()) * 60_000L;
        long finMs = inicioMs + duracion.getValue() * 60 * 60_000L;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservas")
                .whereEqualTo("fecha", fecha.getValue())
                .get()
                .addOnSuccessListener(reservasSnap -> {
                    boolean plazaOcupada = false;

                    for (QueryDocumentSnapshot doc : reservasSnap) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        if (reserva.getPlazaId().getId() == plazaId.getValue()) {
                            long reservaInicio = reserva.getHoraInicio().getHoraInicio();
                            long reservaFin = reserva.getHoraInicio().getHoraFin();

                            if (inicioMs < reservaFin && reservaInicio < finMs) {
                                plazaOcupada = true;
                                break;
                            }
                        }
                    }

                    if (plazaOcupada) {
                        mensajeError.setValue("La plaza ya ha sido reservada");
                        cargando.setValue(false);
                    } else {
                        guardarReserva(inicioMs, finMs, context);
                    }
                })
                .addOnFailureListener(e -> {
                    mensajeError.setValue("Error al verificar disponibilidad");
                    cargando.setValue(false);
                });
    }

    /**
     * Guarda una nueva reserva en Firestore
     */
    private void guardarReserva(long inicioMs, long finMs, Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Hora hora = new Hora(inicioMs, finMs);
        Plaza plaza = new Plaza(plazaId.getValue(), tipoPlaza.getValue());

        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth, selectedDay, horaInicio.getValue(), minutosInicio.getValue(), 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long NotificationInicioMs = calendar.getTimeInMillis();

        long NotificationFinMs = NotificationInicioMs + duracion.getValue() * 60 * 60_000L;

        Reserva reserva = new Reserva();
        reserva.setUserId(userId);
        reserva.setFecha(fecha.getValue());
        reserva.setHoraInicio(hora);
        reserva.setPlazaId(plaza);
        reserva.setMatricula(matricula.getValue());

        FirebaseFirestore.getInstance().collection("reservas")
                .add(reserva)
                .addOnSuccessListener(documentReference -> {
                    reserva.setId(documentReference.getId());
                    if (Boolean.TRUE.equals(guardarMatricula.getValue())) {
                        guardarMatriculaEnPerfil(matricula.getValue());
                    }
                    NotificationScheduler.scheduleNotification(context, NotificationInicioMs - 5 * 60_000, 1, "Tu reserva está por empezar", "Faltan 5 minutos para tu reserva.");
                    NotificationScheduler.scheduleNotification(context, NotificationInicioMs, 2, "¡Reserva iniciada!", "Tu reserva ha comenzado.");
                    NotificationScheduler.showInstantNotification(
                            context,
                            100,
                            "Reserva confirmada",
                            "Tu reserva ha sido realizada con éxito."
                    );
                    this.calcularTiempoRestante(reserva, context);
                    reservaExitosa.setValue(true);
                    cargando.setValue(false);
                })
                .addOnFailureListener(e -> {
                    mensajeError.setValue("Error al guardar la reserva");
                    cargando.setValue(false);
                });
    }

    private void guardarMatriculaEnPerfil(String matricula) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Primero obtenemos el perfil para ver si ya existe la matrícula
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> matriculas = new ArrayList<>();

                    // Si ya existe una lista de matrículas, la obtenemos
                    if (documentSnapshot.exists() && documentSnapshot.contains("matriculas")) {
                        List<String> matriculasExistentes = (List<String>) documentSnapshot.get("matriculas");
                        if (matriculasExistentes != null) {
                            matriculas.addAll(matriculasExistentes);
                        }
                    }

                    // Si la matrícula no está en la lista, la añadimos
                    if (!matriculas.contains(matricula)) {
                        matriculas.add(matricula);

                        // Actualizamos el documento en Firestore
                        db.collection("users").document(userId)
                                .update("matriculas", matriculas)
                                .addOnFailureListener(e ->
                                        System.out.println("Error al guardar matrícula: " + e.getMessage()));
                    }
                });
    }

    private void calcularTiempoRestante(Reserva reserva, Context context) {
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

            long tiempoFinReal = calFin.getTimeInMillis();
            long tiempoActualMs = System.currentTimeMillis();
            long tiempoRestanteMs = tiempoFinReal - tiempoActualMs;

            long cincoMinAntes = tiempoFinReal - (5 * 60 * 1000);
            if (cincoMinAntes > tiempoActualMs) {
                NotificationScheduler.scheduleNotification(
                        context,
                        cincoMinAntes,
                        reserva.getId().hashCode() * 10 + 1,
                        "Reserva por finalizar",
                        "Tu reserva termina en 5 minutos."
                );
            }

            if (tiempoFinReal > tiempoActualMs) {
                NotificationScheduler.scheduleNotification(
                        context,
                        tiempoFinReal,
                        reserva.getId().hashCode() * 10 + 2,
                        "Reserva finalizada",
                        "Tu reserva ha finalizado."
                );
            }

            if (tiempoRestanteMs > 0) {
                iniciarTemporizador(tiempoRestanteMs);
            } else {
                tiempoRestante.setValue("Finalizada");
            }
        } catch (ParseException e) {
            tiempoRestante.setValue("En curso");
        }
    }

    /**
     * Calcula la hora de finalización basada en hora inicio y duración
     */
    public HorarioCalculado calcularHorario() {
        if (horaInicio.getValue() == null || minutosInicio.getValue() == null ||
                duracion.getValue() == null) {
            return null;
        }

        int totalMinInicio = horaInicio.getValue() * 60 + minutosInicio.getValue();
        int totalMinFin = totalMinInicio + duracion.getValue() * 60;
        int horaFin = totalMinFin / 60;
        int minFin = totalMinFin % 60;

        return new HorarioCalculado(
                horaInicio.getValue(), minutosInicio.getValue(),
                horaFin, minFin
        );
    }

    public void buscarPlazaDisponible(String tipoPlaza) {
        busquedaIntentada.setValue(false);
        // Asegúrate de que fecha/hora/duración están seteados
        if (fecha.getValue() == null || horaInicio.getValue() == null || duracion.getValue() == null) {
            mensajeError.setValue("Completa todos los datos");
            return;
        }
        cargando.setValue(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservas")
                .whereEqualTo("fecha", fecha.getValue())
                .get()
                .addOnSuccessListener(reservasSnap -> {
                    Set<Long> ocupadas = new HashSet<>();
                    long miInicio = (horaInicio.getValue() * 60L + minutosInicio.getValue()) * 60_000L;
                    long miFin = miInicio + duracion.getValue() * 60 * 60_000L;

                    for (QueryDocumentSnapshot doc : reservasSnap) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        if (tipoPlaza.equals(reserva.getPlazaId().getTipo())) {
                            long reservaInicio = reserva.getHoraInicio().getHoraInicio();
                            long reservaFin = reserva.getHoraInicio().getHoraFin();
                            if (miInicio < reservaFin && reservaInicio < miFin) {
                                ocupadas.add(reserva.getPlazaId().getId());
                            }
                        }
                    }

                    // Suponiendo que tienes un método para obtener todas las plazas de ese tipo
                    List<Plaza> plazasTipo = obtenerPlazasPorTipo(tipoPlaza);
                    Plaza libre = null;
                    for (Plaza p : plazasTipo) {
                        if (!ocupadas.contains(p.getId())) {
                            libre = p;
                            break;
                        }
                    }
                    if (libre != null) {
                        plazaId.setValue(libre.getId());
                        this.tipoPlaza.setValue(tipoPlaza);
                        plazaDisponible.setValue(libre);
                    } else {
                        plazaDisponible.setValue(null);
                    }
                    busquedaIntentada.setValue(true);
                    cargando.setValue(false);
                })
                .addOnFailureListener(e -> {
                    mensajeError.setValue("Error al buscar plaza");
                    cargando.setValue(false);
                    plazaDisponible.setValue(null);
                    busquedaIntentada.setValue(true);
                });
    }

    private List<Plaza> obtenerPlazasPorTipo(String tipo) {
        List<Plaza> plazas = new java.util.ArrayList<>();
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
     * Obtiene el texto descriptivo para el tipo de plaza
     */
    public String getTipoPlazaTexto(String tipo) {
        if (tipo == null) return "Estándar";

        switch (tipo) {
            case "minusvalido": return "Minusválidos";
            case "electrico": return "Vehículo eléctrico";
            case "normal": default: return "Estándar";
            case "moto": return "Moto";
        }
    }


    /**
     * Reinicia todos los datos para una nueva reserva
     */
    public void reiniciar() {
        fecha.setValue(null);
        horaInicio.setValue(null);
        minutosInicio.setValue(0);
        duracion.setValue(1);
        plazaId.setValue(null);
        tipoPlaza.setValue(null);
        plazasOcupadas.setValue(new HashSet<>());
        mensajeError.setValue(null);
        cargando.setValue(false);
        reservaExitosa.setValue(false);
        plazaDisponible.setValue(null);
        busquedaIntentada.setValue(false);
        guardarMatricula.setValue(false);
    }

    /**
     * Clase auxiliar para representar el horario calculado
     */
    public static class HorarioCalculado {
        private final int horaInicio;
        private final int minInicio;
        private final int horaFin;
        private final int minFin;

        public HorarioCalculado(int horaInicio, int minInicio, int horaFin, int minFin) {
            this.horaInicio = horaInicio;
            this.minInicio = minInicio;
            this.horaFin = horaFin;
            this.minFin = minFin;
        }

        public int getHoraInicio() { return horaInicio; }
        public int getMinInicio() { return minInicio; }
        public int getHoraFin() { return horaFin; }
        public int getMinFin() { return minFin; }

        public String getHoraInicioFormateada() {
            return String.format("%02d:%02d", horaInicio, minInicio);
        }

        public String getHoraFinFormateada() {
            return String.format("%02d:%02d", horaFin, minFin);
        }
    }

    public void limpiarMensajeError() {
        mensajeError.setValue(null);
    }

    public void setSelectedDate(int year, int month, int day) {
        this.selectedYear = year;
        this.selectedMonth = month;
        this.selectedDay = day;
    }

}