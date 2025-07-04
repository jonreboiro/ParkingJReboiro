package com.lksnext.parkingJReboiro.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.domain.ReservaConTiempo;
import com.lksnext.parkingJReboiro.util.PlazaUtils;
import com.lksnext.parkingJReboiro.view.fragment.PlanoParkingDialogFragment;

import java.util.List;

public class ReservasActivasAdapter extends RecyclerView.Adapter<ReservasActivasAdapter.ViewHolder> {

    private List<ReservaConTiempo> reservasActivas;

    public ReservasActivasAdapter(List<ReservaConTiempo> reservasActivas) {
        this.reservasActivas = reservasActivas;
    }

    public void actualizarReservas(List<ReservaConTiempo> nuevasReservas) {
        this.reservasActivas = nuevasReservas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reserva_activa, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReservaConTiempo reservaConTiempo = reservasActivas.get(position);

        // Formatear los datos de la reserva
        String tipoPlaza = PlazaUtils.getTipoPlazaFormal(reservaConTiempo.getReserva().getPlazaId().getTipo());
        holder.tvPlazaReserva.setText("Plaza: " + tipoPlaza + "-" +
                reservaConTiempo.getReserva().getPlazaId().getId());
        holder.tvFechaReserva.setText("Fecha: " + reservaConTiempo.getReserva().getFecha());

        // Formatear horario
        long horaInicioMs = reservaConTiempo.getReserva().getHoraInicio().getHoraInicio();
        long horaFinMs = reservaConTiempo.getReserva().getHoraInicio().getHoraFin();

        int horaInicio = (int)(horaInicioMs / (60 * 60 * 1000));
        int minInicio = (int)((horaInicioMs % (60 * 60 * 1000)) / (60 * 1000));

        int horaFin = (int)(horaFinMs / (60 * 60 * 1000));
        int minFin = (int)((horaFinMs % (60 * 60 * 1000)) / (60 * 1000));

        holder.tvHorarioReserva.setText(String.format("Horario: %02d:%02d - %02d:%02d",
                horaInicio, minInicio, horaFin, minFin));

        // Mostrar estado/tiempo restante
        holder.tvEstadoReserva.setText("Estado: " + reservaConTiempo.getTiempoRestante());

        String matricula = reservaConTiempo.getReserva().getMatricula();
        if (matricula != null && !matricula.isEmpty()) {
            holder.tvMatriculaReserva.setText("Matrícula: " + matricula);
            holder.tvMatriculaReserva.setVisibility(View.VISIBLE);
        } else {
            holder.tvMatriculaReserva.setVisibility(View.GONE);
        }
        // Configurar botón de ver en plano
        holder.btnVerPlano.setOnClickListener(v -> {
            int planta = (reservaConTiempo.getReserva().getPlazaId().getId() >= 13) ? 0 : 1;
            PlanoParkingDialogFragment dialog = PlanoParkingDialogFragment.newInstance(
                    planta,
                    reservaConTiempo.getReserva().getPlazaId().getId(),
                    reservaConTiempo.getReserva().getPlazaId().getTipo());
            dialog.show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), "PlanoParkingDialog");
        });
    }

    @Override
    public int getItemCount() {
        return reservasActivas != null ? reservasActivas.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlazaReserva, tvFechaReserva, tvHorarioReserva, tvEstadoReserva, tvMatriculaReserva;
        ImageButton btnVerPlano;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlazaReserva = itemView.findViewById(R.id.tvPlazaReserva);
            tvFechaReserva = itemView.findViewById(R.id.tvFechaReserva);
            tvHorarioReserva = itemView.findViewById(R.id.tvHorarioReserva);
            tvEstadoReserva = itemView.findViewById(R.id.tvEstadoReserva);
            tvMatriculaReserva = itemView.findViewById(R.id.tvMatriculaReserva);
            btnVerPlano = itemView.findViewById(R.id.btnVerPlano);
        }
    }
}