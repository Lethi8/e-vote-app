package com.example.vota.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vota.R;
import com.example.vota.model.CardItem;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.Holder> {
    private final List<CardItem> items;

    public CardAdapter(List<CardItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        CardItem x = items.get(position);
        h.badge.setText(x.badge);
        h.title.setText(x.title);
        h.body.setText(x.body);
        h.action.setText(x.action);
        h.itemView.setOnClickListener(v -> {
            if (x.onClick != null) x.onClick.run();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView badge, title, body, action;

        Holder(View v) {
            super(v);
            badge = v.findViewById(R.id.txtBadge);
            title = v.findViewById(R.id.txtTitle);
            body = v.findViewById(R.id.txtBody);
            action = v.findViewById(R.id.txtAction);
        }
    }
}