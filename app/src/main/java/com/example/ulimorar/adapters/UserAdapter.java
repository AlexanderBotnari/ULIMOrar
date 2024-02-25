package com.example.ulimorar.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ulimorar.R;
import com.example.ulimorar.databinding.UserListItemBinding;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.entities.enums.UserRole;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<User> newList) {
        users.clear();
        users.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @NotNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        UserListItemBinding userListItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.user_list_item, parent, false);
        return new UserViewHolder(userListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull UserAdapter.UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.userListItemBinding.setUser(user);
        holder.userListItemBinding.executePendingBindings();
        if (user.getRole().equals(UserRole.ADMIN.toString())){
            holder.userListItemBinding.iconImageView.setImageResource(R.drawable.ic_admin);
        }else{
            holder.userListItemBinding.iconImageView.setImageResource(R.drawable.ic_users);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        private final UserListItemBinding userListItemBinding;

        public UserViewHolder(UserListItemBinding userListItemBinding) {
            super(userListItemBinding.getRoot());
            this.userListItemBinding = userListItemBinding;
        }
    }
}
