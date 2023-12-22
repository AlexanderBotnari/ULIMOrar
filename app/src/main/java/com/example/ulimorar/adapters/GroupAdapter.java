package com.example.ulimorar.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ulimorar.R;
import com.example.ulimorar.entities.Group;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>{

    private List<Group> groups;
    private Context context;

    public GroupAdapter(List<Group> groups, Context context) {
        this.groups = groups;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chair_group_list_item, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GroupAdapter.GroupViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.groupNameTextView.setText(group.getGroupName());
        if (group.getGroupSymbol().length() == 2 || group.getGroupSymbol().length() == 3){
            holder.groupSymbolTextView.setPadding(0, 0, 0, 0);
        }
        holder.groupSymbolTextView.setText(group.getGroupSymbol());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Group item clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder{

        private TextView groupNameTextView;
        private TextView groupSymbolTextView;

        public GroupViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.chairGroupNameTextView);
            groupSymbolTextView = itemView.findViewById(R.id.chairGroupSymbol);
        }
    }
}
