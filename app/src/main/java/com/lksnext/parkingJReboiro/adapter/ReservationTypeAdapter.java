package com.lksnext.parkingJReboiro.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.parkingJReboiro.R;

import java.util.List;

public class ReservationTypeAdapter extends RecyclerView.Adapter<ReservationTypeAdapter.TypeViewHolder> {

    public interface OnTypeSelectedListener {
        void onTypeSelected(String type);
    }

    private final List<TypeItem> types;
    private final OnTypeSelectedListener listener;
    private int selectedPosition = 0;

    public ReservationTypeAdapter(List<TypeItem> types, OnTypeSelectedListener listener) {
        this.types = types;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation_type, parent, false);
        return new TypeViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TypeViewHolder holder, int position) {
        TypeItem item = types.get(position);
        holder.tvName.setText(item.name);
        holder.ivIcon.setImageResource(item.iconResId);

        // Destacar la tarjeta seleccionada
        Context context = holder.itemView.getContext();
        if (selectedPosition == position) {
            holder.cardView.setCardBackgroundColor(context.getColor(R.color.md_theme_primaryContainer));
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            listener.onTypeSelected(item.type);
        });
    }

    @Override
    public int getItemCount() {
        return types.size();
    }

    public static class TypeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivIcon;
        TextView tvName;

        public TypeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardType);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }

    // Clase auxiliar para los tipos
    public static class TypeItem {
        public final String type;
        public final String name;
        public final int iconResId;

        public TypeItem(String type, String name, int iconResId) {
            this.type = type;
            this.name = name;
            this.iconResId = iconResId;
        }
    }
}