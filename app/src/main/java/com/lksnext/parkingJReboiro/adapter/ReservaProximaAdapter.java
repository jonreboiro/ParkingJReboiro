package com.lksnext.parkingJReboiro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.domain.Plaza;
import com.lksnext.parkingJReboiro.domain.Reserva;

import java.util.List;

public class ReservaProximaAdapter extends RecyclerView.Adapter<ReservaProximaAdapter.ViewHolder> {

    private List<Reserva> reservas;
    private OnReservaCancelarListener listener;

    public interface OnReservaCancelarListener {
        void onCancelarReserva(Reserva reserva, int position);
    }

    public ReservaProximaAdapter(List<Reserva> reservas, OnReservaCancelarListener listener) {
        this.reservas = reservas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reserva_proxima, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reserva reserva = reservas.get(position);

        // Obtener tipo de plaza
        String tipoPlaza = getTipoPorId(reserva.getPlazaId());
        holder.tvPlaza.setText("Plaza: " + tipoPlaza + "-" + reserva.getPlazaId().getId());
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

        holder.tvEstado.setText("Estado: Programada");

        // Configurar botón de cancelar
        holder.btnCancelar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelarReserva(reserva, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservas.size();
    }

    private String getTipoPorId(Plaza plaza) {
        return plaza.getTipo();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaza, tvFecha, tvHorario, tvEstado;
        MaterialButton btnCancelar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaza = itemView.findViewById(R.id.tvPlazaReserva);
            tvFecha = itemView.findViewById(R.id.tvFechaReserva);
            tvHorario = itemView.findViewById(R.id.tvHorarioReserva);
            tvEstado = itemView.findViewById(R.id.tvEstadoReserva);
            btnCancelar = itemView.findViewById(R.id.btnCancelarReserva);
        }
    }
}