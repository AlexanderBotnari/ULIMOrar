package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ulimorar.R;
import com.example.ulimorar.adapters.FacultyAdapter;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.entities.enums.UserRole;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.example.ulimorar.viewmodels.FacultyViewModel;
import com.example.ulimorar.viewmodels.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class FacultyActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private FloatingActionButton addFacultyButton;
    private ImageButton facultyAddImageButton;
    private ImageView emptyImageView;

    private String authenticatedUserEmail;

    private UserViewModel userViewModel;
    private FacultyViewModel facultyViewModel;

    private List<Faculty> faculties;
    private FacultyAdapter facultyAdapter;
    private RecyclerView recyclerView;

    private TextInputLayout facultyNameInputLayout;
    private TextInputLayout facultyDescriptionInputLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private AlertDialog alertDialog;

    private Uri selectedImageUri;

    private Menu activityMenu;

    private boolean isAdminFlag = false;

    private Faculty facultyToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.faculty_activity_title);
        setContentView(R.layout.activity_faculty);

        authenticatedUserEmail = getIntent().getStringExtra("currentUserEmail");

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        facultyViewModel = new ViewModelProvider(this).get(FacultyViewModel.class);

        recyclerView = findViewById(R.id.facultyRecyclerView);
        faculties = new ArrayList<>();
        facultyAdapter = new FacultyAdapter(faculties, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(facultyAdapter);

        emptyImageView = findViewById(R.id.emptyImageView);
        addFacultyButton = findViewById(R.id.addFacultyFloatingButton);
        addFacultyButton.setVisibility(View.INVISIBLE);
        addFacultyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(R.string.add_faculty_dialog_title, true, null);
            }
        });

        // Initialize the ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        facultyAddImageButton.setImageResource(R.drawable.ic_succes);
                        Intent data = result.getData();
                        if (data != null) {
                            selectedImageUri = data.getData();
                        }
                    }else{
                        facultyAddImageButton.setImageResource(R.drawable.ic_failed);
                    }
                });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                facultyViewModel.getFaculties();
            }
        });

        facultyViewModel.getFacultyListLiveData().observe(this, new Observer<List<Faculty>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<Faculty> faculties) {
                if (!faculties.isEmpty()){
                    emptyImageView.setVisibility(View.GONE);
                    facultyAdapter.setFaculties(faculties);
                    facultyAdapter.notifyDataSetChanged();

                    swipeRefreshLayout.setRefreshing(false);
                }else {
                    emptyImageView.setVisibility(View.VISIBLE);
                }
            }
        });

        userViewModel.getUserByEmailLiveData(authenticatedUserEmail).observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {

                if (userIsAdmin(user)){
                    isAdminFlag = true;
                    facultyAdapter.setAdmin(true);
                    addFacultyButton.setVisibility(View.VISIBLE);
                    try {
                        activityMenu.findItem(R.id.users).setVisible(true);
                    }catch (Exception ignored){

                    }
                }else{
                    isAdminFlag = false;
                    facultyAdapter.setAdmin(false);
                }
            }
        });

    }

    public void openDialog(int dialogTitle, boolean isAddDialog, Integer itemPosition) {
        View alertDialogCustomView = LayoutInflater.from(this).inflate(R.layout.add_edit_faculty_dialog, null);

        TextView titleTextView = alertDialogCustomView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(dialogTitle);
        facultyNameInputLayout = alertDialogCustomView.findViewById(R.id.facultyNameInputLayout);
        facultyDescriptionInputLayout = alertDialogCustomView.findViewById(R.id.facultyDescriptionInputLayout);
        facultyAddImageButton = alertDialogCustomView.findViewById(R.id.facultyAddImageButton);

        facultyAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        if (!isAddDialog){
            facultyToUpdate = facultyAdapter.getFaculties().get(itemPosition);
            facultyNameInputLayout.getEditText().setText(facultyToUpdate.getFacultyName());
            facultyDescriptionInputLayout.getEditText().setText(facultyToUpdate.getFacultyDescription());
        }

        GetDialogsStandardButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String facultyName = facultyNameInputLayout.getEditText().getText().toString();
                String facultyDescription = facultyDescriptionInputLayout.getEditText().getText().toString();

                boolean isValid = true;

                if (facultyName.isEmpty()){
                    facultyNameInputLayout.setError(getString(R.string.empty_faculty_name_error));
                    isValid = false;
                }else {
                    facultyNameInputLayout.setError(null);
                }

                if (facultyDescription.isEmpty()){
                    facultyDescriptionInputLayout.setError(getString(R.string.empty_faculty_description_error));
                    isValid = false;
                }else{
                    facultyDescriptionInputLayout.setError(null);
                }

                if (isValid) {
                    if (isAddDialog){
                        facultyViewModel.addFaculty(FacultyActivity.this, facultyName,
                                facultyDescription, selectedImageUri, alertDialog);
                    }else{
                        facultyViewModel.editFaculty(facultyToUpdate.getId(), facultyName,
                                facultyDescription, facultyToUpdate, selectedImageUri,
                                FacultyActivity.this, alertDialog);
                    }
                }
            }
        });

        GetDialogsStandardButtons.getCancelButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    private boolean userIsAdmin(User user){
        return user.getRole().equals(UserRole.ADMIN.toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        facultyAdapter.setAuthenticatedUserEmail(authenticatedUserEmail);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // simulate initial swipe refresh
                        try {
                            Thread.sleep(2000); // simulate refresh for 2 seconds
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // process data in background
                        facultyViewModel.getFaculties();

                        // update UI in main thread
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // stop refresh after load data
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        activityMenu = menu;
        activityMenu.findItem(R.id.faculty).setVisible(false);
        activityMenu.findItem(R.id.users).setVisible(isAdminFlag);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.users:
                startActivity(new Intent(FacultyActivity.this, UsersActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            case R.id.account:
                startActivity(new Intent(FacultyActivity.this, AccountActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}