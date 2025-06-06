package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.lksnext.parkingplantilla.R;


public class MainFragment extends Fragment {
    public MainFragment() {
        // Es necesario un constructor vacio

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        MaterialButton btnRealizarReserva = view.findViewById(R.id.btnRealizarReserva);
        btnRealizarReserva.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mainFragment_to_realizarReservaFragment);
        });

        MaterialButton btnConsultarReservas = view.findViewById(R.id.btnConsultarReservas);
        btnConsultarReservas.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_mainFragment_to_consultarReservasFragment);
        });

        return view;
    }


}
