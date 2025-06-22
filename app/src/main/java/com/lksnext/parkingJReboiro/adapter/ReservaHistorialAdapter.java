package com.lksnext.parkingJReboiro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.domain.Reserva;

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
                .inflate(R.layout.item_reserva_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);

        // Obtener tipo de plaza directamente usando el método getTipo()
        String tipoPlaza = reserva.getPlazaId().getTipo();
        holder.tvPlaza.setText("Plaza: " + tipoPlaza + " - " + reserva.getPlazaId().getId());
        holder.tvFecha.setText("Fecha: " + reserva.getFecha());

        // Formatear horario
        long horaInicioMs = reserva.getHoraInicio().getHoraInicio();
        long horaFinMs = reserva.getHoraInicio().getHoraFin();

        int horaInicio = (int)(horaInicioMs / (60 * 60 * 1000));
        int minInicio = (int)((horaInicioMs % (60 * 60 * 1000)) / (60 * 1000));

        int horaFin = (int)(horaFinMs / (60 * 60 * 1000));
        int minFin = (int)((horaFinMs % (60 * 60 * 1000)) / (60 * 1000));

        holder.tvHorario.setText(String.format("Horario: %02d:%02d - %02d:%02d",
                horaInicio, minInicio, horaFin, minFin));
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaza, tvFecha, tvHorario;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaza = itemView.findViewById(R.id.tvPlazaReserva);
            tvFecha = itemView.findViewById(R.id.tvFechaReserva);
            tvHorario = itemView.findViewById(R.id.tvHorarioReserva);
        }
    }
}