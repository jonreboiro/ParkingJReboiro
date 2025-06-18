package com.lksnext.parkingJReboiro.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.parkingJReboiro.R;
import com.lksnext.parkingJReboiro.domain.Plaza;

import java.util.List;
import java.util.Set;

public class PlazaAdapter extends RecyclerView.Adapter<PlazaAdapter.PlazaViewHolder> {
    private List<Plaza> plazas;
    private Set<Long> ocupadas;
    private OnPlazaClickListener listener;

    public interface OnPlazaClickListener {
        void onPlazaClick(Plaza plaza);
    }

    public PlazaAdapter(List<Plaza> plazas, Set<Long> ocupadas, OnPlazaClickListener listener) {
        this.plazas = plazas;
        this.ocupadas = ocupadas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlazaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plaza, parent, false);
        return new PlazaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlazaViewHolder holder, int position) {
        Plaza plaza = plazas.get(position);
        holder.bind(plaza, ocupadas.contains(plaza.getId()), listener);
    }

    @Override
    public int getItemCount() {
        return plazas.size();
    }

    static class PlazaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTipo;
        View root;

        PlazaViewHolder(View itemView) {
            super(itemView);
            root = itemView;
            tvTipo = itemView.findViewById(R.id.tvTipoPlaza);
        }

        void bind(Plaza plaza, boolean ocupada, OnPlazaClickListener listener) {
            tvTipo.setText(plaza.getTipo()); // Aquí puedes poner el icono/emoji según tipo
            root.setEnabled(!ocupada);
            root.setBackgroundColor(ocupada ? Color.RED : Color.GREEN);
            root.setOnClickListener(v -> {
                if (!ocupada) listener.onPlazaClick(plaza);
            });
        }
    }
}