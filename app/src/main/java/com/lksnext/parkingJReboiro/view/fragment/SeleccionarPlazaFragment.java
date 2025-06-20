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
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.domain.Reserva;
import com.lksnext.parkingJReboiro.view.ParkingMapView;

import java.util.HashSet;
import java.util.Set;

public class SeleccionarPlazaFragment extends Fragment implements ParkingMapView.OnPlazaSelectedListener {
    private ParkingMapView parkingMapView;
    private Set<Long> plazasOcupadas = new HashSet<>();
    private MaterialButton btnConfirmarPlaza;

    private String fecha;
    private int horaInicio, minutosInicio, duracion;
    private long plazaSeleccionada; // Cambiado de Long a long
    private String tipoPlaza = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seleccionar_plaza, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            fecha = getArguments().getString("fecha");
            horaInicio = getArguments().getInt("horaInicio");
            minutosInicio = getArguments().getInt("minutosInicio", 0);
            duracion = getArguments().getInt("duracion");
        } else {
            minutosInicio = 0;
        }

        // Mostrar fecha y hora seleccionada
        TextView tvFecha = view.findViewById(R.id.tvFechaSeleccionada);
        TextView tvHora = view.findViewById(R.id.tvHoraSeleccionada);
        tvFecha.setText("Fecha: " + fecha);

        // Calcula hora fin
        int totalMinInicio = horaInicio * 60 + minutosInicio;
        int totalMinFin = totalMinInicio + duracion * 60;
        int horaFin = totalMinFin / 60;
        int minFin = totalMinFin % 60;
        String horaInicioStr = String.format("%02d:%02d", horaInicio, minutosInicio);
        String horaFinStr = String.format("%02d:%02d", horaFin, minFin);
        tvHora.setText("Hora: " + horaInicioStr + " - " + horaFinStr);

        // Inicializar ParkingMapView
        parkingMapView = view.findViewById(R.id.parkingMapView);
        parkingMapView.setOnPlazaSelectedListener(this);

        // Configurar botón confirmar
        btnConfirmarPlaza = view.findViewById(R.id.btnConfirmarPlaza);
        btnConfirmarPlaza.setEnabled(false);
        btnConfirmarPlaza.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("fecha", fecha);
            args.putInt("horaInicio", horaInicio);
            args.putInt("minutosInicio", minutosInicio);
            args.putInt("duracion", duracion);
            args.putLong("plazaId", plazaSeleccionada);
            args.putString("tipoPlaza", tipoPlaza); // Añadir el tipo de plaza
            Navigation.findNavController(view).navigate(
                    R.id.action_seleccionarPlazaFragment_to_confirmarDetallesFragment, args);
        });
        // Cargar plazas ocupadas
        cargarReservasYActualizar();
    }

    private void cargarReservasYActualizar() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservas")
                .whereEqualTo("fecha", fecha)
                .get()
                .addOnSuccessListener(reservasSnap -> {
                    plazasOcupadas.clear();

                    // Calcula inicio y fin en milisegundos desde medianoche
                    long miInicio = (horaInicio * 60L + minutosInicio) * 60_000L;
                    long miFin = miInicio + duracion * 60 * 60_000L; // duracion en horas

                    for (QueryDocumentSnapshot doc : reservasSnap) {
                        Reserva reserva = doc.toObject(Reserva.class);
                        long reservaInicio = reserva.getHoraInicio().getHoraInicio();
                        long reservaFin = reserva.getHoraInicio().getHoraFin();

                        // Solapan si: miInicio < reservaFin && reservaInicio < miFin
                        if (miInicio < reservaFin && reservaInicio < miFin) {
                            plazasOcupadas.add(reserva.getPlazaId().getId());
                        }
                    }

                    // Actualizar el mapa con las plazas ocupadas
                    if (parkingMapView != null) {
                        parkingMapView.setPlazasOcupadas(plazasOcupadas);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar reservas", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onPlazaSelected(long plazaId, String tipo) {
        // Se ejecuta cuando el usuario selecciona una plaza en el mapa
        plazaSeleccionada = plazaId;
        tipoPlaza = tipo;
        btnConfirmarPlaza.setEnabled(true);

        // Mostrar información sobre la plaza seleccionada
        Toast.makeText(getContext(),
                "Plaza " + plazaId + " (" + tipo + ") seleccionada",
                Toast.LENGTH_SHORT).show();
    }
}