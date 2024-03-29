package com.example.ulimorar.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ulimorar.R;
import com.example.ulimorar.databinding.FacultyListItemBinding;
import com.example.ulimorar.fragments.interfaces.BottomSheetListener;
import com.example.ulimorar.activities.ChairActivity;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.activities.FacultyActivity;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.viewmodels.FacultyViewModel;
import com.squareup.picasso.Picasso;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class FacultyAdapter extends RecyclerView.Adapter<FacultyAdapter.FacultyViewHolder> implements BottomSheetListener {

    private List<Faculty> faculties;
    private FacultyActivity facultyActivity;
    private boolean isAdmin;
    private String authenticatedUserEmail;
    private DeleteBottomSheetFragment bottomSheetFragment;

    private int facultyPositionToDelete;
    private FacultyViewModel facultyViewModel;

    public FacultyAdapter(List<Faculty> faculties, FacultyActivity facultyActivity) {
        this.faculties = faculties;
        this.facultyActivity = facultyActivity;
        facultyViewModel = new ViewModelProvider(facultyActivity).get(FacultyViewModel.class);
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

    public void setFaculties(List<Faculty> faculties) {
        this.faculties = faculties;
    }

    @NonNull
    @NotNull
    @Override
    public FacultyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        FacultyListItemBinding facultyListItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.faculty_list_item, parent, false);
        return new FacultyViewHolder(facultyListItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FacultyAdapter.FacultyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Faculty faculty = faculties.get(position);
        holder.facultyListItemBinding.setFaculty(faculty);

        Picasso.get().load(faculty.getFacultyPosterPath()).placeholder(R.drawable.ulim_logo).into(holder.facultyImageView);

        holder.viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FacultyView", faculty.getFacultyName()+" "+position);
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
                    facultyPositionToDelete = position;
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
    public void onButtonDelete(View view) {
        facultyViewModel.deleteFaculty(faculties.get(facultyPositionToDelete), facultyActivity);
    }

    public static class FacultyViewHolder extends RecyclerView.ViewHolder{

        private ImageView facultyImageView;
        private Button viewButton;
        private Button editFacultyButton;
        private Button deleteFacultyButton;

        private FacultyListItemBinding facultyListItemBinding;

        public FacultyViewHolder(FacultyListItemBinding facultyListItemBinding) {
            super(facultyListItemBinding.getRoot());
            this.facultyListItemBinding = facultyListItemBinding;
            facultyImageView = itemView.findViewById(R.id.facultyImageView);
            viewButton = itemView.findViewById(R.id.viewButton);
            editFacultyButton = itemView.findViewById(R.id.editFacultyButton);
            deleteFacultyButton = itemView.findViewById(R.id.deleteFacultyButton);
        }
    }
}
