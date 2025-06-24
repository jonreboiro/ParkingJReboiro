package com.lksnext.parkingJReboiro.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.viewmodel.MainViewModel;

public class MainFragment extends Fragment {

    private MainViewModel viewModel;
    private MaterialButton btnRealizarReserva;
    private MaterialButton btnConsultarReservas;
    private ProgressBar progressBar;

    public MainFragment() {
        // Es necesario un constructor vacío
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Inicializar vistas
        btnRealizarReserva = view.findViewById(R.id.btnRealizarReserva);
        btnConsultarReservas = view.findViewById(R.id.btnConsultarReservas);
        progressBar = view.findViewById(R.id.progressBar);

        // Configurar el ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        // Observar cambios en el estado de autenticación
        viewModel.isAuthenticated.observe(getViewLifecycleOwner(), isAuthenticated -> {
            btnRealizarReserva.setEnabled(isAuthenticated);
            btnConsultarReservas.setEnabled(isAuthenticated);

            if (!isAuthenticated) {
                // Si no está autenticado, podríamos navegar a login o mostrar un mensaje
                Toast.makeText(requireContext(), "Necesita iniciar sesión para usar estas funciones", Toast.LENGTH_SHORT).show();
            }
        });

        // Observar estado de carga
        viewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observar mensajes de error
        viewModel.errorMessage.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                viewModel.clearErrorMessage();
            }
        });

        // Configurar eventos de click
        btnRealizarReserva.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mainFragment_to_realizarReservaFragment);
        });

        btnConsultarReservas.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mainFragment_to_consultarReservasFragment);
        });

        // Comprobar estado de autenticación al iniciar
        viewModel.refreshUserData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refreshUserData();
    }
}