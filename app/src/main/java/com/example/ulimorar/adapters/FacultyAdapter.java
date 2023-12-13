package com.example.ulimorar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ulimorar.R;
import com.example.ulimorar.entities.Faculty;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class FacultyAdapter extends RecyclerView.Adapter<FacultyAdapter.FacultyViewHolder> {

    private List<Faculty> faculties;

    public FacultyAdapter(List<Faculty> faculties) {
        this.faculties = faculties;
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
    }

    @Override
    public int getItemCount() {
        return faculties.size();
    }

    public static class FacultyViewHolder extends RecyclerView.ViewHolder{

        private ImageView facultyImageView;
        private TextView facultyNameTextView;
        private TextView facultyDescriptionTextView;

        public FacultyViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            facultyImageView = itemView.findViewById(R.id.facultyImageView);
            facultyNameTextView = itemView.findViewById(R.id.facultyNameTextView);
            facultyDescriptionTextView = itemView.findViewById(R.id.facultyDescriptionTextView);
        }
    }
}
