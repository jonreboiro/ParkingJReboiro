package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lksnext.parkingplantilla.adapter.NotificacionesAdapter;
import com.lksnext.parkingplantilla.databinding.FragmentNotificacionesBinding;
import com.lksnext.parkingplantilla.domain.Notificacion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificacionesFragment extends Fragment {

    private FragmentNotificacionesBinding binding;
    private NotificacionesAdapter adapter;
    private List<Notificacion> notificaciones;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificacionesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar la lista y adaptador
        notificaciones = obtenerNotificacionesDePrueba(); // Reemplazar con datos reales
        adapter = new NotificacionesAdapter(notificaciones);

        // Configurar RecyclerView
        binding.rvNotificaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotificaciones.setAdapter(adapter);

        // Mostrar texto de "sin notificaciones" si la lista está vacía
        actualizarVistaVacia();

        // Configurar botones
        binding.btnMarcarLeidas.setOnClickListener(v -> marcarTodasComoLeidas());
        binding.btnEliminarTodas.setOnClickListener(v -> eliminarTodasLasNotificaciones());
    }

    private void actualizarVistaVacia() {
        if (notificaciones.isEmpty()) {
            binding.tvSinNotificaciones.setVisibility(View.VISIBLE);
            binding.rvNotificaciones.setVisibility(View.GONE);
            binding.btnMarcarLeidas.setEnabled(false);
            binding.btnEliminarTodas.setEnabled(false);
        } else {
            binding.tvSinNotificaciones.setVisibility(View.GONE);
            binding.rvNotificaciones.setVisibility(View.VISIBLE);
            binding.btnMarcarLeidas.setEnabled(true);
            binding.btnEliminarTodas.setEnabled(true);
        }
    }

    private void marcarTodasComoLeidas() {
        for (Notificacion notificacion : notificaciones) {
            notificacion.setLeida(true);
        }
        adapter.notifyDataSetChanged();
    }

    private void eliminarTodasLasNotificaciones() {
        notificaciones.clear();
        adapter.notifyDataSetChanged();
        actualizarVistaVacia();
    }

    // Método temporal para datos de prueba
    private List<Notificacion> obtenerNotificacionesDePrueba() {
        List<Notificacion> lista = new ArrayList<>();
        lista.add(new Notificacion(1, "Reserva confirmada", "Tu reserva para hoy ha sido confirmada", new Date(), false));
        lista.add(new Notificacion(2, "Recordatorio", "Tu reserva comienza en 30 minutos", new Date(), false));
        return lista;
    }
}