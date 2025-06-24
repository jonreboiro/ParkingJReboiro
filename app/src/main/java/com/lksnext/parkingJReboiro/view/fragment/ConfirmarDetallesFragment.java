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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.viewmodel.NuevaReservaViewModel;

public class ConfirmarDetallesFragment extends Fragment {
    private NuevaReservaViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirmar_detalles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(NuevaReservaViewModel.class);

        // Mostrar detalles desde el ViewModel
        ((TextView) view.findViewById(R.id.tvFecha)).setText("Fecha: " + viewModel.getFecha().getValue());
        NuevaReservaViewModel.HorarioCalculado horario = viewModel.calcularHorario();
        if (horario != null) {
            ((TextView) view.findViewById(R.id.tvHoraInicio)).setText("Hora de inicio: " + horario.getHoraInicioFormateada());
            ((TextView) view.findViewById(R.id.tvHoraFin)).setText("Hora de finalización: " + horario.getHoraFinFormateada());
        }
        ((TextView) view.findViewById(R.id.tvDuracion)).setText("Duración: " + viewModel.getDuracion().getValue() + " horas");
        ((TextView) view.findViewById(R.id.tvIdPlaza)).setText("ID de plaza: " + viewModel.getPlazaId().getValue());
        ((TextView) view.findViewById(R.id.tvTipoPlaza)).setText("Tipo de plaza: " + viewModel.getTipoPlazaTexto(viewModel.getTipoPlaza().getValue()));

        view.findViewById(R.id.btnCancelar).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_confirmarDetallesFragment_to_mainFragment);
        });

        view.findViewById(R.id.btnVolverSeleccion).setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_confirmarDetallesFragment_to_seleccionarPlazaFragment);
        });

        MaterialButton btnConfirmar = view.findViewById(R.id.btnConfirmar);
        btnConfirmar.setOnClickListener(v -> {
            btnConfirmar.setEnabled(false);
            viewModel.verificarYGuardarReserva();
        });

        // Observa resultado de la reserva
        viewModel.getReservaExitosa().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Reserva completada con éxito", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigate(R.id.action_confirmarDetallesFragment_to_mainFragment);
                viewModel.reiniciar();
            }
        });
        viewModel.getMensajeError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                btnConfirmar.setEnabled(true);
            }
        });
    }
}