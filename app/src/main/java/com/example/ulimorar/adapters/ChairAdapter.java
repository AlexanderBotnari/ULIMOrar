package com.example.ulimorar.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ulimorar.R;
import com.example.ulimorar.entities.Chair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChairAdapter extends RecyclerView.Adapter<ChairAdapter.ChairViewHolder> {

    private List<Chair> chairs;
    private Context context;

    public ChairAdapter(List<Chair> chairs, Context context) {
        this.chairs = chairs;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public ChairViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chair_group_list_item, parent, false);
        return new ChairViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChairAdapter.ChairViewHolder holder, int position) {
        Chair chair = chairs.get(position);

        holder.chairNameTextView.setText(chair.getChairName());
        holder.chairSymbolTextView.setText(chair.getChairSymbol());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Chair item clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return chairs.size();
    }

    public static class ChairViewHolder extends RecyclerView.ViewHolder {

        private TextView chairNameTextView;
        private TextView chairSymbolTextView;

        public ChairViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            chairNameTextView = itemView.findViewById(R.id.chairNameTextView);
            chairSymbolTextView = itemView.findViewById(R.id.chairSymbol);
        }
    }
}
