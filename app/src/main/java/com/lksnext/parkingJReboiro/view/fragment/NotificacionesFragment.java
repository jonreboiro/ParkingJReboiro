package com.lksnext.parkingJReboiro.view.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.parkingJReboiro.adapter.NotificacionesAdapter;
import com.lksnext.parkingJReboiro.databinding.FragmentNotificacionesBinding;
import com.lksnext.parkingJReboiro.domain.Notificacion;
import com.lksnext.parkingJReboiro.notifications.NotificacionesStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificacionesFragment extends Fragment {

    private FragmentNotificacionesBinding binding;
    private NotificacionesAdapter adapter;
    private List<Notificacion> notificaciones;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "notificaciones_prefs";
    private static final String KEY_LEIDAS = "notificaciones_leidas";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificacionesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        notificaciones = NotificacionesStorage.cargarNotificaciones(requireContext(), userId);
        cargarEstadoLeidas();

        adapter = new NotificacionesAdapter(notificaciones, new NotificacionesAdapter.OnNotificacionClickListener() {
            @Override
            public void onNotificacionClick(int position) {
                marcarComoLeida(position);
            }

            @Override
            public void onEliminarClick(int position) {
                eliminarNotificacion(position);
            }
        });

        binding.rvNotificaciones.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNotificaciones.setAdapter(adapter);

        binding.btnMarcarLeidas.setOnClickListener(v -> marcarTodasComoLeidas());
        binding.btnEliminarTodas.setOnClickListener(v -> eliminarTodasLasNotificaciones());

        actualizarVistaVacia();
    }

    private void marcarComoLeida(int position) {
        Notificacion n = notificaciones.get(position);
        if (!n.isLeida()) {
            n.setLeida(true);
            guardarEstadoLeidas();
            adapter.notifyItemChanged(position);
        }
    }

    private void eliminarNotificacion(int position) {
        notificaciones.remove(position);
        guardarEstadoLeidas();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        NotificacionesStorage.guardarLista(requireContext(), notificaciones, userId);
        adapter.notifyItemRemoved(position);
        actualizarVistaVacia();
    }

    private void marcarTodasComoLeidas() {
        for (Notificacion n : notificaciones) n.setLeida(true);
        guardarEstadoLeidas();
        adapter.notifyDataSetChanged();
    }

    private void eliminarTodasLasNotificaciones() {
        notificaciones.clear();
        guardarEstadoLeidas();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        NotificacionesStorage.guardarLista(requireContext(), notificaciones, userId);
        adapter.notifyDataSetChanged();
        actualizarVistaVacia();
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

    private void guardarEstadoLeidas() {
        Set<String> leidas = new HashSet<>();
        for (Notificacion n : notificaciones) {
            if (n.isLeida()) leidas.add(String.valueOf(n.getId()));
        }
        prefs.edit().putStringSet(KEY_LEIDAS, leidas).apply();
    }

    private void cargarEstadoLeidas() {
        Set<String> leidas = prefs.getStringSet(KEY_LEIDAS, new HashSet<>());
        for (Notificacion n : notificaciones) {
            n.setLeida(leidas.contains(String.valueOf(n.getId())));
        }
    }
}