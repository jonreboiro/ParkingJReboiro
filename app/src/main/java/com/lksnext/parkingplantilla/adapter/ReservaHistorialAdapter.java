package com.lksnext.parkingplantilla.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.domain.Reserva;

import java.util.List;

public class ReservaHistorialAdapter extends RecyclerView.Adapter<ReservaHistorialAdapter.ViewHolder> {

    private List<Reserva> reservas;

    public ReservaHistorialAdapter(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reserva, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);
        holder.tvPlaza.setText("Plaza: " + reserva.getPlazaId().getId());
        holder.tvFecha.setText("Fecha: " + reserva.getFecha());
        // Suponiendo que toString() es el método correcto para obtener la hora como string
        holder.tvHorario.setText("Horario: " + reserva.getHoraInicio().toString() + " a " + reserva.getHoraFin().toString());
        // Añadiendo un estado predeterminado
        holder.tvEstado.setText("Estado: Pendiente"); // Ajustar según la lógica de tu aplicación
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaza, tvFecha, tvHorario, tvEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaza = itemView.findViewById(R.id.tvPlazaReserva);
            tvFecha = itemView.findViewById(R.id.tvFechaReserva);
            tvHorario = itemView.findViewById(R.id.tvHorarioReserva);
            tvEstado = itemView.findViewById(R.id.tvEstadoReserva);
        }
    }
}