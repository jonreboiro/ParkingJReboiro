package com.lksnext.parkingJReboiro.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.parkingJReboiro.R;

import java.util.ArrayList;
import java.util.List;

public class MatriculasAdapter extends RecyclerView.Adapter<MatriculasAdapter.MatriculaViewHolder> {

    private List<String> matriculas;
    private OnMatriculaClickListener listener;

    public interface OnMatriculaClickListener {
        void onMatriculaDelete(String matricula);
    }

    public MatriculasAdapter(OnMatriculaClickListener listener) {
        this.matriculas = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public MatriculaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_matricula, parent, false);
        return new MatriculaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatriculaViewHolder holder, int position) {
        String matricula = matriculas.get(position);
        holder.tvMatricula.setText(matricula);
        holder.btnDelete.setOnClickListener(v -> {
            // Añadir una animación sutil al eliminar
            v.animate().alpha(0.5f).scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(100);
                        if (listener != null) {
                            // Mensaje de confirmación para eliminar
                            new AlertDialog.Builder(v.getContext())
                                    .setTitle("Eliminar matrícula")
                                    .setMessage("¿Deseas eliminar la matrícula " + matricula + "?")
                                    .setPositiveButton("Eliminar", (dialog, which) -> listener.onMatriculaDelete(matricula))
                                    .setNegativeButton("Cancelar", null)
                                    .show();
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return matriculas.size();
    }

    public void setMatriculas(List<String> matriculas) {
        this.matriculas.clear();
        if (matriculas != null) {
            this.matriculas.addAll(matriculas);
        }
        notifyDataSetChanged();
    }

    static class MatriculaViewHolder extends RecyclerView.ViewHolder {
        TextView tvMatricula;
        ImageButton btnDelete;

        MatriculaViewHolder(View itemView) {
            super(itemView);
            tvMatricula = itemView.findViewById(R.id.tvMatricula);
            btnDelete = itemView.findViewById(R.id.btnDeleteMatricula);
        }
    }
}