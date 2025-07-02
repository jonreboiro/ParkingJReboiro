package com.lksnext.parkingJReboiro.view.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.adapter.ReservationTypeAdapter;
import com.lksnext.parkingJReboiro.viewmodel.NuevaReservaViewModel;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class RealizarReservaFragment extends Fragment {
    private EditText etFecha, etHoraInicio;
    private Slider sliderDuracion;
    private Integer selectedYear = null, selectedMonth = null, selectedDay = null, selectedHour = null, selectedMinute = null;

    private int duracionSeleccionada = 1;
    private NuevaReservaViewModel viewModel;

    private RadioGroup rgModoReserva;
    private RecyclerView rvTipoPlaza;
    private ReservationTypeAdapter typeAdapter;
    private String tipoPlazaSeleccionado = "normal";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_realizar_reserva, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            viewModel = new ViewModelProvider(requireActivity()).get(NuevaReservaViewModel.class);
            viewModel.reiniciar();

            etFecha = view.findViewById(R.id.etFecha);
            etHoraInicio = view.findViewById(R.id.etHoraInicio);
            sliderDuracion = view.findViewById(R.id.sliderDuracion);

            etFecha.setOnClickListener(v -> mostrarDatePicker());
            etHoraInicio.setOnClickListener(v -> mostrarTimePicker());

            sliderDuracion.addOnChangeListener((slider, value, fromUser) -> {
                duracionSeleccionada = (int) value;
            });

            // Observa errores del ViewModel
            viewModel.getMensajeError().observe(getViewLifecycleOwner(), msg -> {
                if (msg != null) Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                etFecha.postDelayed(() -> viewModel.limpiarMensajeError(), 100);
            });

            rgModoReserva = view.findViewById(R.id.rgModoReserva);
            rvTipoPlaza = view.findViewById(R.id.rvTipoPlaza);
            rvTipoPlaza.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

            List<ReservationTypeAdapter.TypeItem> tipos = Arrays.asList(
                    new ReservationTypeAdapter.TypeItem("normal", "Normal", R.drawable.ic_parking),
                    new ReservationTypeAdapter.TypeItem("electrico", "Eléctrico", R.drawable.ic_electric_car),
                    new ReservationTypeAdapter.TypeItem("minusvalido", "Minusválido", R.drawable.ic_accessible),
                    new ReservationTypeAdapter.TypeItem("moto", "Moto", R.drawable.ic_motorcycle)
            );

            typeAdapter = new ReservationTypeAdapter(tipos, type -> {
                viewModel.setPlazaTipoSeleccionada(type); // LiveData en el ViewModel
            });
            rvTipoPlaza.setAdapter(typeAdapter);

            // Mostrar/ocultar según modo
            rgModoReserva.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbAuto) {
                    rvTipoPlaza.setVisibility(View.VISIBLE);
                } else {
                    rvTipoPlaza.setVisibility(View.GONE);
                }
            });



            MaterialButton btnContinuar = view.findViewById(R.id.btnContinuar);
            // Botón continuar
            btnContinuar.setOnClickListener(v -> {
                if (selectedYear == null || selectedMonth == null || selectedDay == null) {
                    Toast.makeText(getContext(), "Selecciona una fecha", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedHour == null || selectedMinute == null) {
                    Toast.makeText(getContext(), "Selecciona una hora", Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.setFecha(etFecha.getText().toString());
                viewModel.setHoraInicio(selectedHour);
                viewModel.setMinutosInicio(selectedMinute);
                viewModel.setDuracion(duracionSeleccionada);

                if (rgModoReserva.getCheckedRadioButtonId() == R.id.rbAuto) {
                    // Reserva automática
                    viewModel.buscarPlazaDisponible(tipoPlazaSeleccionado);
                } else {
                    // Reserva manual (ya implementado)
                    Navigation.findNavController(view).navigate(R.id.action_realizarReservaFragment_to_seleccionarPlazaFragment);
                }
            });

            // Observa resultado de búsqueda automática
            viewModel.getPlazaDisponible().observe(getViewLifecycleOwner(), plaza -> {
                if (plaza != null) {
                    // Plaza encontrada: navega a confirmación
                    Navigation.findNavController(requireView()).navigate(R.id.action_realizarReservaFragment_to_confirmarDetallesFragment);
                } else if (viewModel.getBusquedaIntentada().getValue() == Boolean.TRUE) {
                    // No encontrada: muestra diálogo
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Sin plazas disponibles")
                            .setMessage("No hay plazas disponibles de ese tipo. ¿Quieres seleccionar manualmente?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                Navigation.findNavController(requireView()).navigate(R.id.action_realizarReservaFragment_to_seleccionarPlazaFragment);
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                }
            });
    }

    private void mostrarDatePicker() {
        Calendar hoy = Calendar.getInstance();
        Calendar maxDate = (Calendar) hoy.clone();
        maxDate.add(Calendar.DAY_OF_MONTH, 7);

        DatePickerDialog datePicker = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedYear = year;
                    selectedMonth = month;
                    selectedDay = dayOfMonth;
                    etFecha.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year));
                    viewModel.setSelectedDate(year, month, dayOfMonth); // <-- Añade esto
                },
                hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH));
        datePicker.getDatePicker().setMinDate(hoy.getTimeInMillis());
        datePicker.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePicker.show();
    }

    private void mostrarTimePicker() {
        Calendar ahora = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    etHoraInicio.setText(String.format("%02d:%02d", hourOfDay, minute));
                },
                ahora.get(Calendar.HOUR_OF_DAY), ahora.get(Calendar.MINUTE), true);
        timePicker.show();
    }
}