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
import com.example.ulimorar.activities.TimetableActivity;
import com.example.ulimorar.entities.Chair;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.entities.Group;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>{

    private Map<String, Group> groups;
    private Context context;
    private Faculty currentFaculty;
    private Chair currentChair;

    private boolean isAdmin;
    private String authenticatedUserEmail;

    public GroupAdapter(Map<String, Group> groups, Context context, Faculty currentFaculty, Chair currentChair) {
        this.groups = groups;
        this.context = context;
        this.currentFaculty = currentFaculty;
        this.currentChair = currentChair;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void setAuthenticatedUserEmail(String authenticatedUserEmail) {
        this.authenticatedUserEmail = authenticatedUserEmail;
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Group> groups) {
        this.groups = groups;
    }

    @NonNull
    @NotNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chair_group_list_item, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GroupAdapter.GroupViewHolder holder, @SuppressLint("RecyclerView") int position) {
        List<String> keys = new ArrayList<>(groups.keySet());
        String groupId = keys.get(position);

        Group group = groups.get(groupId);

        assert group != null;
        holder.groupNameTextView.setText(group.getGroupName());

        if (group.getGroupSymbol().length() == 2 || group.getGroupSymbol().length() == 3){
            holder.groupSymbolTextView.setPadding(0, 0, 0, 0);
        }
        holder.groupSymbolTextView.setText(group.getGroupSymbol());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, TimetableActivity.class);
                intent.putExtra("groupFromIntent", group);
                intent.putExtra("currentFaculty", currentFaculty);
                intent.putExtra("chairId", currentChair.getId());
                intent.putExtra("userIsAdmin", isAdmin);
                intent.putExtra("currentUserEmail", authenticatedUserEmail);
                context.startActivity(intent);
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
