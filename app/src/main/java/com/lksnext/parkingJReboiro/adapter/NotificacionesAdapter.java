package com.lksnext.parkingJReboiro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.domain.Notificacion;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificacionesAdapter extends RecyclerView.Adapter<NotificacionesAdapter.ViewHolder> {

    private List<Notificacion> notificaciones;
    private SimpleDateFormat dateFormat;

    public NotificacionesAdapter(List<Notificacion> notificaciones) {
        this.notificaciones = notificaciones;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion notificacion = notificaciones.get(position);
        holder.tvTitulo.setText(notificacion.getTitulo());
        holder.tvMensaje.setText(notificacion.getMensaje());
        holder.tvFecha.setText(dateFormat.format(notificacion.getFecha()));

        // Opcional: marcar visualmente las leídas/no leídas
        if (!notificacion.isLeida()) {
            holder.itemView.setBackgroundResource(R.drawable.bg_item_no_leido);
        } else {
            holder.itemView.setBackgroundResource(0);
        }
    }

    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    public void actualizarNotificaciones(List<Notificacion> nuevasNotificaciones) {
        this.notificaciones = nuevasNotificaciones;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvMensaje, tvFecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvNotificacionTitulo);
            tvMensaje = itemView.findViewById(R.id.tvNotificacionMensaje);
            tvFecha = itemView.findViewById(R.id.tvNotificacionFecha);
        }
    }
}