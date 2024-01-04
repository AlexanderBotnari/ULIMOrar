package com.example.ulimorar.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ulimorar.R;
import com.example.ulimorar.fragments.interfaces.BottomSheetListener;
import com.example.ulimorar.activities.ChairActivity;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.activities.FacultyActivity;
import com.example.ulimorar.entities.Faculty;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class FacultyAdapter extends RecyclerView.Adapter<FacultyAdapter.FacultyViewHolder> implements BottomSheetListener {

    private List<Faculty> faculties;
    private FacultyActivity facultyActivity;
    private boolean isAdmin;
    private String authenticatedUserEmail;
    private DeleteBottomSheetFragment bottomSheetFragment;
    private Faculty faculty;

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

    public List<Faculty> getFaculties() {
        return faculties;
    }

    @NonNull
    @NotNull
    @Override
    public FacultyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.faculty_list_item, parent, false);
        return new FacultyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FacultyAdapter.FacultyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        faculty = faculties.get(position);

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
                facultyActivity.startActivity(intent);
            }
        });

        if (isAdmin){
            holder.editFacultyButton.setVisibility(View.VISIBLE);
            holder.deleteFacultyButton.setVisibility(View.VISIBLE);
            holder.editFacultyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    facultyActivity.openDialog(R.string.edit_faculty_dialog_title, false, position);
                }
            });

            holder.deleteFacultyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetFragment = new DeleteBottomSheetFragment();
                    bottomSheetFragment.show(facultyActivity.getSupportFragmentManager(), bottomSheetFragment.getTag());
                    bottomSheetFragment.setBottomSheetListener(FacultyAdapter.this);
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

    @Override
    public void onButtonCancel() {
        bottomSheetFragment.dismiss();
    }

    @Override
    public void onButtonDelete() {
        facultyActivity.deleteFaculty(faculty);
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
