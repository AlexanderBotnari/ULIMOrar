package com.example.ulimorar.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ulimorar.R;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.entities.enums.UserRole;
import com.google.android.material.snackbar.Snackbar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> users;

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

    @NonNull
    @NotNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UserAdapter.UserViewHolder holder, int position) {
        User user = users.get(position);

        if (user.getRole().equals(UserRole.ADMIN.toString())){
            holder.iconImageView.setImageResource(R.drawable.ic_admin);
        }else{
            holder.iconImageView.setImageResource(R.drawable.ic_users);
        }

        holder.firstNameTextView.setText(user.getFirstName());
        holder.lastNameTextView.setText(user.getLastName());
        holder.emailTextView.setText(user.getEmail());
        holder.idnpTextView.setText(user.getIdnp());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "Password for user: "+user.getEmail()+" is : "+user.getPassword();
                Snackbar snackbar = Snackbar.make(view, message, 5000);
                snackbar.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        private TextView firstNameTextView;
        private TextView lastNameTextView;
        private TextView emailTextView;
        private TextView idnpTextView;
        private ImageView iconImageView;

        public UserViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            firstNameTextView = itemView.findViewById(R.id.userFirstNameTextView);
            lastNameTextView = itemView.findViewById(R.id.userLastNameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            idnpTextView = itemView.findViewById(R.id.idnpTextView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
        }
    }
}
