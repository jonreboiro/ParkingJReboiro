package com.lksnext.parkingJReboiro.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.domain.Hora;
import com.lksnext.parkingJReboiro.domain.Plaza;
import com.lksnext.parkingJReboiro.domain.Reserva;

public class ConfirmarDetallesFragment extends Fragment {
    private String fecha;
    private int horaInicio, minutosInicio, duracion;
    private long plazaId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirmar_detalles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener datos de los argumentos
        if (getArguments() != null) {
            fecha = getArguments().getString("fecha");
            horaInicio = getArguments().getInt("horaInicio");
            minutosInicio = getArguments().getInt("minutosInicio", 0);
            duracion = getArguments().getInt("duracion");
            plazaId = getArguments().getLong("plazaId");
            String tipoPlaza = getArguments().getString("tipoPlaza", getTipoPorId(plazaId));


            // Calcular hora fin
            int totalMinInicio = horaInicio * 60 + minutosInicio;
            int totalMinFin = totalMinInicio + duracion * 60;
            int horaFin = totalMinFin / 60;
            int minFin = totalMinFin % 60;

            // Mostrar detalles en el layout
            ((TextView) view.findViewById(R.id.tvFecha)).setText("Fecha: " + fecha);
            ((TextView) view.findViewById(R.id.tvHoraInicio)).setText("Hora de inicio: " + String.format("%02d:%02d", horaInicio, minutosInicio));
            ((TextView) view.findViewById(R.id.tvDuracion)).setText("Duración: " + duracion + " horas");
            ((TextView) view.findViewById(R.id.tvHoraFin)).setText("Hora de finalización: " + String.format("%02d:%02d", horaFin, minFin));
            ((TextView) view.findViewById(R.id.tvIdPlaza)).setText("ID de plaza: " + plazaId);
            ((TextView) view.findViewById(R.id.tvTipoPlaza)).setText("Tipo de plaza: " + tipoPlaza);
        }

        // Configurar botón cancelar
        view.findViewById(R.id.btnCancelar).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_confirmarDetallesFragment_to_mainFragment);
        });

        // Configurar botón volver a selección de plazas
        view.findViewById(R.id.btnVolverSeleccion).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("fecha", fecha);
            args.putInt("horaInicio", horaInicio);
            args.putInt("minutosInicio", minutosInicio);
            args.putInt("duracion", duracion);

            Navigation.findNavController(view).navigate(
                    R.id.action_confirmarDetallesFragment_to_seleccionarPlazaFragment, args);
        });

        // Configurar botón confirmar
        view.findViewById(R.id.btnConfirmar).setOnClickListener(v -> {
            verificarYGuardarReserva(v);
        });
    }

    private String getTipoPorId(long id) {
        switch ((int)id) {
            case 1: return "A";
            case 2: return "B";
            case 3: return "C";
            default: return "Estándar";
        }
    }

    private void verificarYGuardarReserva(View view) {
        MaterialButton btnConfirmar = view.findViewById(R.id.btnConfirmar);
        btnConfirmar.setEnabled(false);

        // Calcular tiempos en milisegundos
        long inicioMs = (horaInicio * 60L + minutosInicio) * 60_000L;
        long finMs = inicioMs + duracion * 60 * 60_000L;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservas")
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnSuccessListener(reservasSnap -> {
                    boolean plazaOcupada = false;

                    // Verificar si la plaza ya está ocupada en ese horario
                    for (QueryDocumentSnapshot doc : reservasSnap) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        if (reserva.getPlazaId().getId() == plazaId) {
                            long reservaInicio = reserva.getHoraInicio().getHoraInicio();
                            long reservaFin = reserva.getHoraInicio().getHoraFin();

                            // Comprobar solapamiento
                            if (inicioMs < reservaFin && reservaInicio < finMs) {
                                plazaOcupada = true;
                                break;
                            }
                        }
                    }

                    if (plazaOcupada) {
                        Toast.makeText(getContext(), "La plaza ya ha sido reservada", Toast.LENGTH_SHORT).show();
                        btnConfirmar.setEnabled(true);
                    } else {
                        guardarReserva(view, inicioMs, finMs);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al verificar disponibilidad", Toast.LENGTH_SHORT).show();
                    btnConfirmar.setEnabled(true);
                });
    }

    private void guardarReserva(View view, long inicioMs, long finMs) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String tipoPlaza = getArguments().getString("tipoPlaza", getTipoPorId(plazaId)); // Obtener tipo de los argumentos

        // Crear objetos para guardar en Firestore
        Hora hora = new Hora(inicioMs, finMs);
        Plaza plaza = new Plaza(plazaId, tipoPlaza);

        Reserva reserva = new Reserva();
        reserva.setUserId(userId);
        reserva.setFecha(fecha);
        reserva.setHoraInicio(hora);
        reserva.setPlazaId(plaza);

        FirebaseFirestore.getInstance().collection("reservas")
                .add(reserva)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Reserva completada con éxito", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigate(R.id.action_confirmarDetallesFragment_to_mainFragment);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al guardar la reserva", Toast.LENGTH_SHORT).show();
                    view.findViewById(R.id.btnConfirmar).setEnabled(true);
                });
    }
}