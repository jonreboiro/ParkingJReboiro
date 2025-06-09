package com.lksnext.parkingJReboiro.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.lksnext.parkingJReboiro.R;

public class ConsultarReservasFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_consultar_reservas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar el RecyclerView para reservas futuras
        RecyclerView rvReservasFuturas = view.findViewById(R.id.rvReservasFuturas);
        rvReservasFuturas.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Aquí añadirías tu adaptador para las reservas futuras
        // rvReservasFuturas.setAdapter(new ReservasFuturasAdapter(listaDeReservas));

        // Configurar el botón para navegar al historial de reservas
        MaterialButton btnHistorialReservas = view.findViewById(R.id.btnHistorialReservas);
        btnHistorialReservas.setOnClickListener(v -> {
            // Navegar al historial de reservas usando la acción definida en nav_graph.xml
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_consultarReservasFragment_to_historialReservasFragment);
        });
    }
}