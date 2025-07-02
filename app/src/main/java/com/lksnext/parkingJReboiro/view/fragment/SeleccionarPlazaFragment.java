package com.lksnext.parkingJReboiro.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.view.ParkingMapView;
import com.lksnext.parkingJReboiro.viewmodel.NuevaReservaViewModel;

import java.util.Set;

public class SeleccionarPlazaFragment extends Fragment implements ParkingMapView.OnPlazaSelectedListener {
    private ParkingMapView parkingMapView;
    private MaterialButton btnConfirmarPlaza;
    private TextView tvPlazaSeleccionada;
    private NuevaReservaViewModel viewModel;

    private int plantaSeleccionada = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seleccionar_plaza, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(NuevaReservaViewModel.class);

        tvPlazaSeleccionada = view.findViewById(R.id.tvPlazaSeleccionada);
        parkingMapView = view.findViewById(R.id.parkingMapView);
        parkingMapView.setOnPlazaSelectedListener(this);

        Spinner spinnerPlanta = view.findViewById(R.id.spinnerPlanta);
        spinnerPlanta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                plantaSeleccionada = (position == 0) ? 0 : -1;
                parkingMapView.setPlanta(plantaSeleccionada);
                viewModel.cargarPlazasOcupadas(plantaSeleccionada);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        MaterialButton btnVolverAtras = view.findViewById(R.id.btnVolverAtras);
        btnVolverAtras.setOnClickListener(v -> Navigation.findNavController(requireView()).navigateUp());

        MaterialButton btnCancelarReserva = view.findViewById(R.id.btnCancelarReserva);
        btnCancelarReserva.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cancelar reserva")
                    .setMessage("¿Estás seguro de que quieres cancelar el proceso de reserva?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        Navigation.findNavController(requireView()).navigate(
                                R.id.action_seleccionarPlazaFragment_to_mainFragment);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        btnConfirmarPlaza = view.findViewById(R.id.btnConfirmarPlaza);
        btnConfirmarPlaza.setEnabled(false);
        btnConfirmarPlaza.setOnClickListener(v -> {
            String tipoPlaza = viewModel.getTipoPlaza().getValue();
            if ("minusvalido".equals(tipoPlaza) || "electrico".equals(tipoPlaza)) {
                String mensaje = "";
                if ("minusvalido".equals(tipoPlaza)) {
                    mensaje = "Estás a punto de reservar una plaza para personas con movilidad reducida. Recuerda que es obligatorio disponer de la Tarjeta Europea de Estacionamiento en caso de requerirse.";
                } else if ("electrico".equals(tipoPlaza)) {
                    mensaje = "Has seleccionado una plaza con punto de carga para vehículos eléctricos. Solo debe utilizarse mientras el vehículo esté en proceso de carga.";
                }
                new AlertDialog.Builder(requireContext())
                        .setTitle("Advertencia")
                        .setMessage(mensaje)
                        .setPositiveButton("Continuar", (dialog, which) -> {
                            Bundle args = new Bundle();
                            args.putBoolean("desdeAutomatico", false);
                            Navigation.findNavController(requireView()).navigate(
                                    R.id.action_seleccionarPlazaFragment_to_confirmarDetallesFragment, args);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            } else {
                Bundle args = new Bundle();
                args.putBoolean("desdeAutomatico", false);
                Navigation.findNavController(requireView()).navigate(
                        R.id.action_seleccionarPlazaFragment_to_confirmarDetallesFragment, args);
            }
        });

        // Observa plazas ocupadas y errores
        viewModel.getPlazasOcupadas().observe(getViewLifecycleOwner(), plazasOcupadas -> {
            if (parkingMapView != null && plazasOcupadas != null) {
                parkingMapView.setPlazasOcupadas(plazasOcupadas);
            }
        });
        viewModel.getMensajeError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });

        // Cargar plazas ocupadas desde el ViewModel
        viewModel.cargarPlazasOcupadas(0);

        // Mostrar info de fecha/hora
        TextView tvFecha = view.findViewById(R.id.tvFechaSeleccionada);
        TextView tvHora = view.findViewById(R.id.tvHoraSeleccionada);
        tvFecha.setText("Fecha: " + viewModel.getFecha().getValue());
        NuevaReservaViewModel.HorarioCalculado horario = viewModel.calcularHorario();
        if (horario != null) {
            tvHora.setText("Hora: " + horario.getHoraInicioFormateada() + " - " + horario.getHoraFinFormateada());
        }
    }

    @Override
    public void onPlazaSelected(long plazaId, String tipo) {
        viewModel.seleccionarPlaza(plazaId, tipo);
        btnConfirmarPlaza.setEnabled(true);
        String tipoTexto = viewModel.getTipoPlazaTexto(tipo);
        tvPlazaSeleccionada.setText("Plaza seleccionada: " + plazaId + " (" + tipoTexto + ")");
    }
}