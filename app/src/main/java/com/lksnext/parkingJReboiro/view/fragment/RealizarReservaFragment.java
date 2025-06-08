package com.lksnext.parkingJReboiro.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.lksnext.parkingJReboiro.R;

public class RealizarReservaFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_realizar_reserva, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar el botón continuar
        MaterialButton btnContinuar = view.findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(v -> {
            // Navegar al fragmento de seleccionar plaza
            Navigation.findNavController(view).navigate(R.id.action_realizarReservaFragment_to_seleccionarPlazaFragment);
        });
    }
}
