package com.lksnext.parkingJReboiro.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.adapter.PlazaAdapter;
import com.lksnext.parkingJReboiro.domain.Plaza;
import com.lksnext.parkingJReboiro.domain.Reserva;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeleccionarPlazaFragment extends Fragment {
    private RecyclerView rvPlazasParking;
    private PlazaAdapter plazaAdapter;
    private List<Plaza> listaPlazas = new ArrayList<>();
    private Set<Long> plazasOcupadas = new HashSet<>();

    private String fecha;
    private int horaInicio, duracion;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int minutosInicio;
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

        // ... resto de tu código ...
        rvPlazasParking = view.findViewById(R.id.rvPlazasParking);
        rvPlazasParking.setLayoutManager(new GridLayoutManager(getContext(), 4));

        plazaAdapter = new PlazaAdapter(listaPlazas, plazasOcupadas, plaza -> {
            if (!plazasOcupadas.contains(plaza.getId())) {
                Bundle args = new Bundle();
                args.putString("fecha", fecha);
                args.putInt("horaInicio", horaInicio);
                args.putInt("minutosInicio", minutosInicio);
                args.putInt("duracion", duracion);
                args.putLong("plazaId", plaza.getId());
                Navigation.findNavController(view).navigate(R.id.action_seleccionarPlazaFragment_to_confirmarDetallesFragment, args);
            }
        });
        rvPlazasParking.setAdapter(plazaAdapter);

        cargarReservasYActualizar(minutosInicio);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inicializarPlazasFijas();
        return inflater.inflate(R.layout.fragment_seleccionar_plaza, container, false);
    }

    private void cargarReservasYActualizar(int minutosInicio) {
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
                        long reservaInicio = reserva.getHoraInicio().getHoraInicio(); // ms desde medianoche
                        long reservaFin = reserva.getHoraInicio().getHoraFin(); // ms desde medianoche

                        // Solapan si: miInicio < reservaFin && reservaInicio < miFin
                        if (miInicio < reservaFin && reservaInicio < miFin) {
                            plazasOcupadas.add(reserva.getPlazaId().getId());
                        }
                    }
                    plazaAdapter.notifyDataSetChanged();
                });
    }

    private void inicializarPlazasFijas() {
        listaPlazas.clear();
        listaPlazas.add(new Plaza(1L, "A"));
        listaPlazas.add(new Plaza(2L, "B"));
        listaPlazas.add(new Plaza(3L, "C"));
        // Añade todas las plazas fijas aquí
    }
}