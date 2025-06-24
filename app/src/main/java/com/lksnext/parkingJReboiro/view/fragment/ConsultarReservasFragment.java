package com.lksnext.parkingJReboiro.view.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.adapter.ReservaProximaAdapter;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.viewmodel.ReservasViewModel;

import java.util.ArrayList;

public class ConsultarReservasFragment extends Fragment {

    private CardView cvReservaActiva;
    private TextView tvPlazaActiva, tvFechaActiva, tvHoraActiva, tvEstadoActiva;
    private RecyclerView rvReservasFuturas;
    private ProgressBar progressBar;
    private ReservaProximaAdapter adapter;
    private ReservasViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_consultar_reservas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ReservasViewModel.class);

        // Inicializar vistas
        cvReservaActiva = view.findViewById(R.id.cvReservaActiva);
        tvPlazaActiva = view.findViewById(R.id.tvPlazaActiva);
        tvFechaActiva = view.findViewById(R.id.tvFechaActiva);
        tvHoraActiva = view.findViewById(R.id.tvHoraActiva);
        tvEstadoActiva = view.findViewById(R.id.tvEstadoActiva);
        progressBar = view.findViewById(R.id.progressBar);

        // Configurar RecyclerView
        rvReservasFuturas = view.findViewById(R.id.rvReservasFuturas);
        rvReservasFuturas.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReservaProximaAdapter(new ArrayList<>(), this::mostrarDialogoConfirmacionCancelar);
        rvReservasFuturas.setAdapter(adapter);

        // Configurar botón historial
        MaterialButton btnHistorialReservas = view.findViewById(R.id.btnHistorialReservas);
        btnHistorialReservas.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_consultarReservasFragment_to_historialReservasFragment);
        });

        // Observar LiveData
        observeViewModel();

        // Cargar reservas
        viewModel.cargarReservasUsuario();
    }

    private void observeViewModel() {
        // Observar reserva activa
        viewModel.getReservaActiva().observe(getViewLifecycleOwner(), reserva -> {
            if (reserva != null) {
                mostrarReservaActiva(reserva);
            } else {
                ocultarReservaActiva();
            }
        });

        // Observar reservas próximas
        viewModel.getReservasProximas().observe(getViewLifecycleOwner(), reservas -> {
            adapter = new ReservaProximaAdapter(reservas, this::mostrarDialogoConfirmacionCancelar);
            rvReservasFuturas.setAdapter(adapter);
        });

        // Observar tiempo restante
        viewModel.getTiempoRestante().observe(getViewLifecycleOwner(), tiempo -> {
            if (tiempo != null) {
                tvEstadoActiva.setText("Estado: " + tiempo);
            }
        });

        // Observar mensajes de error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observar estado de carga
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observar operación exitosa
        viewModel.getOperacionExitosa().observe(getViewLifecycleOwner(), exitoso -> {
            if (Boolean.TRUE.equals(exitoso)) {
                Toast.makeText(requireContext(), "Reserva cancelada con éxito", Toast.LENGTH_SHORT).show();
                viewModel.resetOperacionExitosa();
            }
        });
    }

    private void mostrarDialogoConfirmacionCancelar(Reserva reserva, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cancelar Reserva")
                .setMessage("¿Está seguro que desea cancelar esta reserva?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    viewModel.cancelarReserva(reserva, position);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void mostrarReservaActiva(Reserva reserva) {
        cvReservaActiva.setVisibility(View.VISIBLE);

        String tipoPlaza = reserva.getPlazaId().getTipo();
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
    }

    private void ocultarReservaActiva() {
        cvReservaActiva.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.cargarReservasUsuario();
    }
}