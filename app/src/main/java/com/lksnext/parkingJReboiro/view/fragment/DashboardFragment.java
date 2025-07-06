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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.adapter.ReservasActivasAdapter;
import com.lksnext.parkingJReboiro.domain.ReservaConTiempo;
import com.lksnext.parkingJReboiro.view.activity.MainActivity;
import com.lksnext.parkingJReboiro.viewmodel.DashboardViewModel;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private RecyclerView rvReservasActivas;
    private TextView tvEmptyReservas;
    private ReservasActivasAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dashboard_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        // Inicializar vistas
        rvReservasActivas = view.findViewById(R.id.rvReservasActivas);
        tvEmptyReservas = view.findViewById(R.id.tvEmptyReservas);
        TextView tvSaludoUsuario = view.findViewById(R.id.tvSaludoUsuario);

        MaterialButton btnNuevaReserva = view.findViewById(R.id.btnNuevaReserva);
        MaterialButton btnMisReservas = view.findViewById(R.id.btnMisReservas);
        MaterialButton btnNotificaciones = view.findViewById(R.id.btnNotificaciones);
        MaterialButton btnPerfil = view.findViewById(R.id.btnPerfil);


        // Configurar RecyclerView
        adapter = new ReservasActivasAdapter(new ArrayList<>());
        rvReservasActivas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReservasActivas.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("username");
                    if (username != null && !username.isEmpty()) {
                        tvSaludoUsuario.setText("¡Saludos, " + username + "!");
                    } else {
                        tvSaludoUsuario.setText("¡Saludos, Usuario!");
                    }
                } else {
                    tvSaludoUsuario.setText("¡Saludos, Usuario!");
                }
            }).addOnFailureListener(e -> {
                tvSaludoUsuario.setText("¡Saludos, Usuario!");
            });
        }

        // Observar cambios en las reservas activas
        viewModel.getReservasActivas().observe(getViewLifecycleOwner(), this::actualizarReservasActivas);

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Aquí podrías mostrar un indicador de carga
        });

        // Configurar botones de acción
        btnNuevaReserva.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_dashboardFragment_to_realizarReservaFragment);
        });

        btnMisReservas.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putInt("selectedMenuId", R.id.reservations);
            navController.navigate(R.id.action_dashboardFragment_to_consultarReservasFragment, args);
        });

        btnNotificaciones.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putInt("selectedMenuId", R.id.notifications);
            navController.navigate(R.id.notificacionesFragment, args);
        });

        btnPerfil.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putInt("selectedMenuId", R.id.person);
            navController.navigate(R.id.profileFragment, args);
        });
    }

    private void actualizarReservasActivas(List<ReservaConTiempo> reservas) {
        if (reservas != null && !reservas.isEmpty()) {
            adapter.actualizarReservas(reservas);
            rvReservasActivas.setVisibility(View.VISIBLE);
            tvEmptyReservas.setVisibility(View.GONE);
        } else {
            rvReservasActivas.setVisibility(View.GONE);
            tvEmptyReservas.setVisibility(View.VISIBLE);
        }
    }
}