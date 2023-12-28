package com.example.ulimorar.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ulimorar.R;
import com.example.ulimorar.activities.ChairActivity;
import com.example.ulimorar.activities.FacultyActivity;
import com.example.ulimorar.entities.Faculty;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class FacultyAdapter extends RecyclerView.Adapter<FacultyAdapter.FacultyViewHolder> {

    private List<Faculty> faculties;
    private FacultyActivity facultyActivity;
    private boolean isAdmin;
    private String authenticatedUserEmail;

    public FacultyAdapter(List<Faculty> faculties, FacultyActivity facultyActivity) {
        this.faculties = faculties;
        this.facultyActivity = facultyActivity;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public void setAuthenticatedUserEmail(String authenticatedUserEmail) {
        this.authenticatedUserEmail = authenticatedUserEmail;
    }

    @NonNull
    @NotNull
    @Override
    public FacultyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faculty_list_item, parent, false);
        return new FacultyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FacultyAdapter.FacultyViewHolder holder, int position) {
        Faculty faculty = faculties.get(position);

        Picasso.get().load(faculty.getFacultyPosterPath()).placeholder(R.drawable.ulim_logo).into(holder.facultyImageView);
        holder.facultyNameTextView.setText(faculty.getFacultyName());
        holder.facultyDescriptionTextView.setText(faculty.getFacultyDescription());

        holder.viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(facultyActivity, ChairActivity.class);
                intent.putExtra("facultyFromIntent", faculty);
                intent.putExtra("userIsAdmin", isAdmin);
                intent.putExtra("currentUserEmail", authenticatedUserEmail);
                facultyActivity.startActivityForResult(intent, FacultyActivity.REQUEST_CODE);
            }
        });

        if (isAdmin){
            holder.editFacultyButton.setVisibility(View.VISIBLE);
            holder.deleteFacultyButton.setVisibility(View.VISIBLE);
            holder.editFacultyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(facultyActivity, "Edit Faculty clicked!", Toast.LENGTH_SHORT).show();
                }
            });

            holder.deleteFacultyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(facultyActivity, "Delete faulty clicked!", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            holder.editFacultyButton.setVisibility(View.INVISIBLE);
            holder.deleteFacultyButton.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return faculties.size();
    }

    public static class FacultyViewHolder extends RecyclerView.ViewHolder{

        private ImageView facultyImageView;
        private TextView facultyNameTextView;
        private TextView facultyDescriptionTextView;
        private Button viewButton;
        private Button editFacultyButton;
        private Button deleteFacultyButton;

        public FacultyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            facultyImageView = itemView.findViewById(R.id.facultyImageView);
            facultyNameTextView = itemView.findViewById(R.id.facultyNameTextView);
            facultyDescriptionTextView = itemView.findViewById(R.id.facultyDescriptionTextView);
            viewButton = itemView.findViewById(R.id.viewButton);
            editFacultyButton = itemView.findViewById(R.id.editFacultyButton);
            deleteFacultyButton = itemView.findViewById(R.id.deleteFacultyButton);
        }
    }
}
