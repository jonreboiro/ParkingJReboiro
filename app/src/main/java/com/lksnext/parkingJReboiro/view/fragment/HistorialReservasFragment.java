package com.lksnext.parkingJReboiro.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.adapter.ReservaHistorialAdapter;
import com.lksnext.parkingJReboiro.viewmodel.ReservasViewModel;

import java.util.ArrayList;

public class HistorialReservasFragment extends Fragment {

    private RecyclerView rvHistorialReservas;
    private ProgressBar progressBar;
    private TextView tvNoReservas;
    private ReservaHistorialAdapter adapter;
    private ReservasViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historial_reservas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModel usando ViewModelProvider con scope de Activity
        viewModel = new ViewModelProvider(requireActivity()).get(ReservasViewModel.class);

        // Inicializar vistas
        rvHistorialReservas = view.findViewById(R.id.rvHistorialReservas);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoReservas = view.findViewById(R.id.tvNoReservas);

        // Configurar RecyclerView
        rvHistorialReservas.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReservaHistorialAdapter(new ArrayList<>());
        rvHistorialReservas.setAdapter(adapter);

        // Configurar botón volver
        MaterialButton btnVolver = view.findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        // Observar ViewModel
        observeViewModel();
    }

    private void observeViewModel() {
        // Observar reservas pasadas
        viewModel.getReservasPasadas().observe(getViewLifecycleOwner(), reservas -> {
            if (reservas.isEmpty()) {
                tvNoReservas.setVisibility(View.VISIBLE);
                rvHistorialReservas.setVisibility(View.GONE);
            } else {
                tvNoReservas.setVisibility(View.GONE);
                rvHistorialReservas.setVisibility(View.VISIBLE);
                adapter = new ReservaHistorialAdapter(reservas);
                rvHistorialReservas.setAdapter(adapter);
            }
        });

        // Observar estado de carga
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                rvHistorialReservas.setVisibility(View.GONE);
                tvNoReservas.setVisibility(View.GONE);
            }
        });

        // Observar mensajes de error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                mostrarMensajeError(error);
            }
        });
    }

    private void mostrarMensajeError(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
        }
        tvNoReservas.setText(mensaje);
        tvNoReservas.setVisibility(View.VISIBLE);
        rvHistorialReservas.setVisibility(View.GONE);
    }
}