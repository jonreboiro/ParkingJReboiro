package com.lksnext.parkingJReboiro.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lksnext.parkingJReboiro.domain.Hora;
import com.lksnext.parkingJReboiro.domain.Plaza;
import com.lksnext.parkingJReboiro.domain.Reserva;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class NuevaReservaViewModel extends ViewModel {
    private Integer selectedYear, selectedMonth, selectedDay;

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
    public void seleccionarPlaza(long id, String tipo) {
        this.plazaId.setValue(id);
        this.tipoPlaza.setValue(tipo);
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
    public void cargarPlazasOcupadas() {
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
                        long reservaInicio = reserva.getHoraInicio().getHoraInicio();
                        long reservaFin = reserva.getHoraInicio().getHoraFin();

                        if (miInicio < reservaFin && reservaInicio < miFin) {
                            plazasOcup.add(reserva.getPlazaId().getId());
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

    /**
     * Verifica y guarda una nueva reserva
     */
    public void verificarYGuardarReserva() {
        if (fecha.getValue() == null || horaInicio.getValue() == null ||
                plazaId.getValue() == null) {
            mensajeError.setValue("Faltan datos para realizar la reserva");
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
                        guardarReserva(inicioMs, finMs);
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
    private void guardarReserva(long inicioMs, long finMs) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Hora hora = new Hora(inicioMs, finMs);
        Plaza plaza = new Plaza(plazaId.getValue(), tipoPlaza.getValue());

        Reserva reserva = new Reserva();
        reserva.setUserId(userId);
        reserva.setFecha(fecha.getValue());
        reserva.setHoraInicio(hora);
        reserva.setPlazaId(plaza);

        FirebaseFirestore.getInstance().collection("reservas")
                .add(reserva)
                .addOnSuccessListener(documentReference -> {
                    reservaExitosa.setValue(true);
                    cargando.setValue(false);
                })
                .addOnFailureListener(e -> {
                    mensajeError.setValue("Error al guardar la reserva");
                    cargando.setValue(false);
                });
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

    /**
     * Obtiene el texto descriptivo para el tipo de plaza
     */
    public String getTipoPlazaTexto(String tipo) {
        if (tipo == null) return "Estándar";

        switch (tipo) {
            case "minusvalido": return "Minusválidos";
            case "electrico": return "Vehículo eléctrico";
            case "normal": default: return "Estándar";
        }
    }

    /**
     * Obtiene el tipo de plaza a partir de su ID
     */
    public String getTipoPorId(long id) {
        switch ((int)id) {
            case 1: return "A";
            case 2: return "B";
            case 3: return "C";
            default: return "Estándar";
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