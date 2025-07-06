package com.lksnext.parkingJReboiro.view.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.viewmodel.NuevaReservaViewModel;

import java.util.ArrayList;
import java.util.List;

public class ConfirmarDetallesFragment extends Fragment {
    private NuevaReservaViewModel viewModel;
    private Spinner spinnerMatriculas;
    private TextInputLayout tilNuevaMatricula;
    private TextInputEditText etNuevaMatricula;
    private CheckBox cbGuardarMatricula;
    private TextView tvErrorMatricula;
    private List<String> listaMatriculas = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirmar_detalles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(NuevaReservaViewModel.class);

        spinnerMatriculas = view.findViewById(R.id.spinnerMatriculas);
        tilNuevaMatricula = view.findViewById(R.id.tilNuevaMatricula);
        etNuevaMatricula = view.findViewById(R.id.etNuevaMatricula);
        cbGuardarMatricula = view.findViewById(R.id.cbGuardarMatricula);
        tvErrorMatricula = view.findViewById(R.id.tvErrorMatricula);

        cargarMatriculas();

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
            Navigation.findNavController(view).navigate(R.id.action_confirmarDetallesFragment_to_realizarReservaFragment);
        });

        boolean desdeAutomatico = requireArguments().getBoolean("desdeAutomatico", false);
        view.findViewById(R.id.btnVolverSeleccion).setOnClickListener(v -> {
            if (desdeAutomatico) {
                Navigation.findNavController(view).navigate(R.id.action_confirmarDetallesFragment_to_realizarReservaFragment);
            } else {
                Navigation.findNavController(view).navigate(R.id.action_confirmarDetallesFragment_to_seleccionarPlazaFragment);
            }
        });

        spinnerMatriculas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccion = listaMatriculas.get(position);
                if ("Introducir otra".equals(seleccion)) {
                    // Mostrar campos para nueva matrícula
                    tilNuevaMatricula.setVisibility(View.VISIBLE);
                    cbGuardarMatricula.setVisibility(View.VISIBLE);
                    viewModel.setMatricula("");
                } else {
                    // Usar matrícula existente
                    tilNuevaMatricula.setVisibility(View.GONE);
                    cbGuardarMatricula.setVisibility(View.GONE);
                    viewModel.setMatricula(seleccion);
                    tvErrorMatricula.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        cbGuardarMatricula.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setGuardarMatricula(isChecked));

        // Listener para validar la matrícula en tiempo real
        etNuevaMatricula.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                viewModel.setMatricula(input);

                if (!input.isEmpty() && !viewModel.esMatriculaEspanolaValida(input)) {
                    tvErrorMatricula.setText("Formato incorrecto. Use 1234ABC o AB-1234-CD");
                    tvErrorMatricula.setVisibility(View.VISIBLE);
                } else {
                    tvErrorMatricula.setVisibility(View.GONE);
                }
            }
        });

        MaterialButton btnConfirmar = view.findViewById(R.id.btnConfirmar);
        btnConfirmar.setOnClickListener(v -> {
            // Validar matrícula si es manual
            if (tilNuevaMatricula.getVisibility() == View.VISIBLE) {
                String input = etNuevaMatricula.getText().toString().trim();
                if (input.isEmpty()) {
                    tvErrorMatricula.setText("Debe ingresar una matrícula");
                    tvErrorMatricula.setVisibility(View.VISIBLE);
                    return;
                }

                if (!viewModel.esMatriculaEspanolaValida(input)) {
                    tvErrorMatricula.setText("Formato incorrecto. Use 1234ABC o AB-1234-CD");
                    tvErrorMatricula.setVisibility(View.VISIBLE);
                    return;
                }
            }

            // Continuar con la confirmación
            btnConfirmar.setEnabled(false);
            viewModel.verificarYGuardarReserva(requireContext());
        });

        // Observa resultado de la reserva
        viewModel.getReservaExitosa().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(getContext(), "Reserva completada con éxito", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigate(R.id.action_confirmarDetallesFragment_to_realizarReservaFragment);
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

    private void cargarMatriculas() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    listaMatriculas.clear();
                    if (documentSnapshot.exists() && documentSnapshot.contains("matriculas")) {
                        List<String> matriculasUsuario = (List<String>) documentSnapshot.get("matriculas");
                        if (matriculasUsuario != null && !matriculasUsuario.isEmpty()) {
                            listaMatriculas.addAll(matriculasUsuario);
                        }
                    }
                    listaMatriculas.add("Introducir otra");

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            listaMatriculas
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMatriculas.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    // En caso de error, usar solo "Introducir otra"
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            listaMatriculas
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerMatriculas.setAdapter(adapter);
                });
    }
}