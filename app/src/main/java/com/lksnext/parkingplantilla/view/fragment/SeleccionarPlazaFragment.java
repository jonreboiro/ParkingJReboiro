package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.lksnext.parkingplantilla.R;

public class SeleccionarPlazaFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seleccionar_plaza, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnConfirmarPlaza = view.findViewById(R.id.btnConfirmarPlaza);

        btnConfirmarPlaza.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_seleccionarPlazaFragment_to_confirmarDetallesFragment);
        });
    }
}