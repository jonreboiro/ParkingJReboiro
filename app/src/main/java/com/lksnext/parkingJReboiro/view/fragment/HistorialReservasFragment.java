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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.adapter.ReservaHistorialAdapter;
import com.lksnext.parkingJReboiro.data.ReservationManager;
import com.lksnext.parkingJReboiro.domain.Reserva;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class HistorialReservasFragment extends Fragment {

    private RecyclerView rvHistorialReservas;
    private ProgressBar progressBar;
    private TextView tvNoReservas;
    private ReservaHistorialAdapter adapter;
    private List<Reserva> reservasPasadas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historial_reservas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        rvHistorialReservas = view.findViewById(R.id.rvHistorialReservas);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoReservas = view.findViewById(R.id.tvNoReservas);

        // Configurar RecyclerView
        rvHistorialReservas.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReservaHistorialAdapter(reservasPasadas);
        rvHistorialReservas.setAdapter(adapter);

        // Configurar botón volver
        MaterialButton btnVolver = view.findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        // Cargar historial de reservas
        cargarHistorialReservas();
    }

    private void cargarHistorialReservas() {
        progressBar.setVisibility(View.VISIBLE);
        rvHistorialReservas.setVisibility(View.GONE);
        tvNoReservas.setVisibility(View.GONE);

        // Obtener usuario actual
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            mostrarMensajeError("Debes iniciar sesión para ver tu historial");
            return;
        }

        String userId = currentUser.getUid();

        // Usar ReservationManager para obtener reservas
        ReservationManager reservationManager = new ReservationManager();
        reservationManager.getReservasDelUsuario(userId, new ReservationManager.ReservasCallback() {
            @Override
            public void onReservasObtenidas(List<Reserva> reservas) {
                progressBar.setVisibility(View.GONE);

                // Clasificar reservas y obtener solo las pasadas
                Map<String, List<Reserva>> reservasClasificadas = reservationManager.clasificarReservas(reservas);
                reservasPasadas.clear();
                reservasPasadas.addAll(reservasClasificadas.get("pasadas"));

                // Ordenar de más reciente a más antigua
                Collections.sort(reservasPasadas, (r1, r2) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    try {
                        Date fecha1 = sdf.parse(r1.getFecha());
                        Date fecha2 = sdf.parse(r2.getFecha());
                        return fecha2.compareTo(fecha1); // Orden descendente (más reciente primero)
                    } catch (ParseException e) {
                        return 0;
                    }
                });

                if (reservasPasadas.isEmpty()) {
                    tvNoReservas.setVisibility(View.VISIBLE);
                } else {
                    rvHistorialReservas.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                mostrarMensajeError("Error al cargar el historial: " + e.getMessage());
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