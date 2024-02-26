package com.example.ulimorar.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ulimorar.R;
import com.example.ulimorar.activities.GroupActivity;
import com.example.ulimorar.entities.Chair;
import com.example.ulimorar.entities.Faculty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChairAdapter extends RecyclerView.Adapter<ChairAdapter.ChairViewHolder> {

    private Map<String, Chair> chairs;
    private Context context;
    private Faculty currentFaculty;
    private boolean isAdmin;
    private String authenticatedUserEmail;

    public ChairAdapter(Map<String, Chair> chairs, Context context, Faculty currentFaculty) {
        this.chairs = chairs;
        this.context = context;
        this.currentFaculty = currentFaculty;
    }

    public void setChairs(Map<String, Chair> chairs) {
        this.chairs = chairs;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void setAuthenticatedUserEmail(String authenticatedUserEmail) {
        this.authenticatedUserEmail = authenticatedUserEmail;
    }

    public Map<String, Chair> getChairs() {
        return chairs;
    }

    @NonNull
    @NotNull
    @Override
    public ChairViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chair_group_list_item, parent, false);
        return new ChairViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChairAdapter.ChairViewHolder holder, @SuppressLint("RecyclerView") int position) {
        List<String> keys = new ArrayList<>(chairs.keySet());
        String key = keys.get(position);

        Chair chair = chairs.get(key);

        if (Objects.requireNonNull(chair).getChairName() != null){
            holder.chairNameTextView.setText(chair.getChairName());
            holder.chairSymbolTextView.setText(chair.getChairSymbol());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GroupActivity.class);
                intent.putExtra("chairFromIntent", chair);
                intent.putExtra("currentFaculty", currentFaculty);
                intent.putExtra("userIsAdmin", isAdmin);
                intent.putExtra("currentUserEmail", authenticatedUserEmail);
                context.startActivity(intent);
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
            chairNameTextView = itemView.findViewById(R.id.chairGroupNameTextView);
            chairSymbolTextView = itemView.findViewById(R.id.chairGroupSymbol);
        }
    }
}
