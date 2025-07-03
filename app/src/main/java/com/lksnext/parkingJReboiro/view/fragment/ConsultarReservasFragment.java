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
import com.lksnext.parkingJReboiro.adapter.ReservasActivasAdapter;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.viewmodel.ReservasViewModel;

import java.util.ArrayList;

public class ConsultarReservasFragment extends Fragment {

    private RecyclerView rvReservasActivas;
    private TextView tvNoReservasActivas;
    private ReservasActivasAdapter adapterActivas;
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
        tvNoReservasActivas = view.findViewById(R.id.tvNoReservasActivas);
        rvReservasActivas = view.findViewById(R.id.rvReservasActivas);
        rvReservasActivas.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapterActivas = new ReservasActivasAdapter(new ArrayList<>());
        rvReservasActivas.setAdapter(adapterActivas);
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
        viewModel.getReservasActivasConTiempo().observe(getViewLifecycleOwner(), reservasActivas -> {
            adapterActivas.actualizarReservas(reservasActivas);
            if (reservasActivas == null || reservasActivas.isEmpty()) {
                rvReservasActivas.setVisibility(View.GONE);
                tvNoReservasActivas.setVisibility(View.VISIBLE);
            } else {
                rvReservasActivas.setVisibility(View.VISIBLE);
                tvNoReservasActivas.setVisibility(View.GONE);
            }
        });

        // Observar reservas próximas
        viewModel.getReservasProximas().observe(getViewLifecycleOwner(), reservas -> {
            adapter = new ReservaProximaAdapter(reservas, this::mostrarDialogoConfirmacionCancelar);
            rvReservasFuturas.setAdapter(adapter);
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
                    viewModel.cancelarReserva(reserva, position, requireContext());
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.cargarReservasUsuario();
    }
}