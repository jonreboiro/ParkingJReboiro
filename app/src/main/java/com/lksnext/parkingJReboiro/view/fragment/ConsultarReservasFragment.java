package com.lksnext.parkingJReboiro.view.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.adapter.ReservaHistorialAdapter;
import com.lksnext.parkingJReboiro.adapter.ReservaProximaAdapter;
import com.lksnext.parkingJReboiro.data.ReservationManager;
import com.lksnext.parkingJReboiro.domain.Plaza;
import com.lksnext.parkingJReboiro.domain.Reserva;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConsultarReservasFragment extends Fragment {

    private CardView cvReservaActiva;
    private TextView tvPlazaActiva, tvFechaActiva, tvHoraActiva, tvEstadoActiva;
    private RecyclerView rvReservasFuturas;
    private List<Reserva> reservasProximas = new ArrayList<>();
    private ReservaProximaAdapter adapter;

    private CountDownTimer countDownTimer;
    private boolean timerRunning = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_consultar_reservas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        cvReservaActiva = view.findViewById(R.id.cvReservaActiva);
        tvPlazaActiva = view.findViewById(R.id.tvPlazaActiva);
        tvFechaActiva = view.findViewById(R.id.tvFechaActiva);
        tvHoraActiva = view.findViewById(R.id.tvHoraActiva);
        tvEstadoActiva = view.findViewById(R.id.tvEstadoActiva);

        // Configurar el RecyclerView para reservas futuras
        rvReservasFuturas = view.findViewById(R.id.rvReservasFuturas);
        rvReservasFuturas.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReservaProximaAdapter(reservasProximas, (reserva, position) -> {
            mostrarDialogoConfirmacionCancelar(reserva, position);
        });
        rvReservasFuturas.setAdapter(adapter);

        // Configurar botón historial
        MaterialButton btnHistorialReservas = view.findViewById(R.id.btnHistorialReservas);
        btnHistorialReservas.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_consultarReservasFragment_to_historialReservasFragment);
        });

        // Cargar reservas del usuario
        cargarReservasUsuario();
    }

    private void mostrarDialogoConfirmacionCancelar(Reserva reserva, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar Reserva")
                .setMessage("¿Está seguro que desea cancelar esta reserva?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    cancelarReserva(reserva, position);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelarReserva(Reserva reserva, int position) {
        ReservationManager reservationManager = new ReservationManager();
        reservationManager.cancelarReserva(
                reserva.getId(),
                aVoid -> {
                    // Éxito al cancelar
                    reservasProximas.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(requireContext(), "Reserva cancelada con éxito", Toast.LENGTH_SHORT).show();
                },
                e -> {
                    // Error al cancelar
                    Toast.makeText(requireContext(), "Error al cancelar la reserva: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void cargarReservasUsuario() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            mostrarMensajeError("Debes iniciar sesión para ver tus reservas");
            return;
        }

        String userId = currentUser.getUid();

        ReservationManager reservationManager = new ReservationManager();
        reservationManager.getReservasDelUsuario(userId, new ReservationManager.ReservasCallback() {
            @Override
            public void onReservasObtenidas(List<Reserva> reservas) {
                Map<String, List<Reserva>> reservasClasificadas = reservationManager.clasificarReservas(reservas);

                // Mostrar reserva actual
                List<Reserva> reservaActual = reservasClasificadas.get("actual");
                if (reservaActual != null && !reservaActual.isEmpty()) {
                    mostrarReservaActiva(reservaActual.get(0));
                } else {
                    ocultarReservaActiva();
                }

                // Mostrar reservas próximas
                reservasProximas.clear();
                List<Reserva> proximas = reservasClasificadas.get("proximas");
                if (proximas != null) {
                    reservasProximas.addAll(proximas);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(Exception e) {
                mostrarMensajeError("Error al cargar reservas: " + e.getMessage());
            }
        });
    }

    private void mostrarReservaActiva(Reserva reserva) {
        cvReservaActiva.setVisibility(View.VISIBLE);

        // Configurar datos
        String tipoPlaza = getTipoPorId(reserva.getPlazaId());
        tvPlazaActiva.setText("Plaza: " + tipoPlaza + "-" + reserva.getPlazaId().getId());
        tvFechaActiva.setText("Fecha: " + reserva.getFecha());

        // Formatear horario
        long horaInicioMs = reserva.getHoraInicio().getHoraInicio();
        long horaFinMs = reserva.getHoraInicio().getHoraFin();

        int horaInicio = (int)(horaInicioMs / (60 * 60 * 1000));
        int minInicio = (int)((horaInicioMs % (60 * 60 * 1000)) / (60 * 1000));

        int horaFin = (int)(horaFinMs / (60 * 60 * 1000));
        int minFin = (int)((horaFinMs % (60 * 60 * 1000)) / (60 * 1000));

        tvHoraActiva.setText(String.format("Horario: %02d:%02d - %02d:%02d",
                horaInicio, minInicio, horaFin, minFin));

        // Calcular tiempo restante
        calcularYMostrarTiempoRestante(reserva);
    }

    private void calcularYMostrarTiempoRestante(Reserva reserva) {
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
            long tiempoRestante = tiempoFinReal - tiempoActualMs;

            if (tiempoRestante > 0) {
                iniciarTemporizador(tiempoRestante);
            } else {
                tvEstadoActiva.setText("Estado: Finalizada");
            }
        } catch (ParseException e) {
            tvEstadoActiva.setText("Estado: En curso");
        }
    }

    private void iniciarTemporizador(long tiempoRestanteMs) {
        detenerTemporizador();

        countDownTimer = new CountDownTimer(tiempoRestanteMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long horas = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                long minutos = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;

                String tiempoRestante = String.format("Estado: En curso - Tiempo restante: %02d:%02d",
                        horas, minutos);
                if (isAdded()) {
                    tvEstadoActiva.setText(tiempoRestante);
                }
            }

            @Override
            public void onFinish() {
                if (isAdded()) {
                    tvEstadoActiva.setText("Estado: Finalizada");
                    timerRunning = false;
                }
            }
        };

        countDownTimer.start();
        timerRunning = true;
    }

    private void ocultarReservaActiva() {
        cvReservaActiva.setVisibility(View.GONE);
        detenerTemporizador();
    }

    private void detenerTemporizador() {
        if (countDownTimer != null && timerRunning) {
            countDownTimer.cancel();
            timerRunning = false;
        }
    }

    private String getTipoPorId(Plaza plaza) {
        return plaza.getTipo();
    }

    private void mostrarMensajeError(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        detenerTemporizador();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarReservasUsuario();
    }
}