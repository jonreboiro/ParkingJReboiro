package com.lksnext.parkingJReboiro.view.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.viewmodel.NuevaReservaViewModel;

import java.util.Calendar;

public class RealizarReservaFragment extends Fragment {
    private EditText etFecha, etHoraInicio;
    private Slider sliderDuracion;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private int duracionSeleccionada = 1;
    private NuevaReservaViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_realizar_reserva, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(NuevaReservaViewModel.class);

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
            viewModel.limpiarMensajeError();
        });

        MaterialButton btnContinuar = view.findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(v -> {
            // Guarda los datos en el ViewModel
            viewModel.setFecha(etFecha.getText().toString());
            viewModel.setHoraInicio(selectedHour);
            viewModel.setMinutosInicio(selectedMinute);
            viewModel.setDuracion(duracionSeleccionada);

            if (viewModel.validarSeleccion(selectedYear, selectedMonth, selectedDay)) {
                Navigation.findNavController(view).navigate(R.id.action_realizarReservaFragment_to_seleccionarPlazaFragment);
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