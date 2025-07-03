package com.lksnext.parkingJReboiro.view.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.view.ParkingMapView;

public class PlanoParkingDialogFragment extends DialogFragment {

    private static final String ARG_PLANTA = "planta";
    private static final String ARG_PLAZA_ID = "plazaId";
    private static final String ARG_TIPO = "tipo";

    public static PlanoParkingDialogFragment newInstance(int planta, long plazaId, String tipo) {
        PlanoParkingDialogFragment frag = new PlanoParkingDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLANTA, planta);
        args.putLong(ARG_PLAZA_ID, plazaId);
        args.putString(ARG_TIPO, tipo);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_plano_parking, null, false);

        assert getArguments() != null;
        int planta = getArguments().getInt(ARG_PLANTA);
        long plazaId = getArguments().getLong(ARG_PLAZA_ID);
        String tipo = getArguments().getString(ARG_TIPO);

        TextView tvPlanta = view.findViewById(R.id.tvPlantaPlano);
        tvPlanta.setText("Planta: " + (planta == 0 ? "0" : "-1"));

        ParkingMapView mapView = view.findViewById(R.id.parkingMapViewPlano);
        mapView.setPlanta(planta);
        mapView.setOnPlazaSelectedListener(null); // No seleccionable en el dialog
        mapView.setPlazasOcupadas(null);
        mapView.setEnabled(false);
        mapView.setClickable(false);
        // Resalta la plaza de la reserva
        mapView.post(() -> {
            mapView.setPlazaSeleccionada(plazaId);
        });
        Dialog dialog = new Dialog(requireContext(), R.style.ThemeOverlay_MaterialComponents_Dialog_Map);
        dialog.setContentView(view);
        return dialog;
    }
}