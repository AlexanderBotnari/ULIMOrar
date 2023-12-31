package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ulimorar.R;
import com.example.ulimorar.adapters.FacultyAdapter;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.entities.enums.UserRole;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FacultyActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private FloatingActionButton addFacultyButton;
    private ImageButton facultyAddImageButton;

    private DatabaseReference facultiesDatabaseReference;
    private DatabaseReference usersDatabaseReference;
    private String authenticatedUserEmail;
    private FirebaseStorage storage;
    private StorageReference storageReference;

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
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        getUserByEmail(authenticatedUserEmail);

        facultiesDatabaseReference = FirebaseDatabase.getInstance().getReference("faculties");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        recyclerView = findViewById(R.id.facultyRecyclerView);
        faculties = new ArrayList<>();
        facultyAdapter = new FacultyAdapter(faculties, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(facultyAdapter);

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
                getFaculties();
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
                        addFaculty(facultyName, facultyDescription);
                    }else{
                        editFaculty(facultyToUpdate.getId(), facultyName, facultyDescription);
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

    private void uploadImageToFirebaseStorage(Uri imageUri, String facultyId) {
        if (imageUri != null) {
            // Create a reference to "images/[filename]"
            StorageReference imageFacultyRef = storageReference.child("faculties/" + facultyId + ".jpg");

            // Upload the file to Firebase Storage
            imageFacultyRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image uploaded successfully
                            Toast.makeText(FacultyActivity.this, R.string.image_uploaded_successful_message, Toast.LENGTH_SHORT).show();

                            // Get the download URL and update the faculty by id in realtime database
                            imageFacultyRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUrl) {
                                    Log.d("DownloadUrl", downloadUrl.toString());
                                    facultiesDatabaseReference.child(facultyId).child("facultyPosterPath").setValue(downloadUrl.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            // Handle unsuccessful uploads
                            Toast.makeText(FacultyActivity.this, R.string.failure_image_upload, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void addFaculty(String facultyName, String facultyDescription) {
            // Generate unique ID for faculty
            String facultyId = facultiesDatabaseReference.push().getKey();

            Faculty faculty = new Faculty(facultyId, facultyName, facultyDescription);

            // Add the faculty to the database
            assert facultyId != null;
            facultiesDatabaseReference.child(facultyId).setValue(faculty).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(FacultyActivity.this, R.string.add_faculty_successful_message, Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(FacultyActivity.this, R.string.add_faculty_failure_message, Toast.LENGTH_SHORT).show();
                    Log.d("FailureAddFaculty", task.getException().getMessage());
                }
            });

            // Upload the selected image to Firebase Storage or perform other actions
            uploadImageToFirebaseStorage(selectedImageUri, facultyId);
            selectedImageUri = null;
    }

    private boolean userIsAdmin(User user){
        return user.getRole().equals(UserRole.ADMIN.toString());
    }

    private void getUserByEmail(String userEmail) {
        Query query = usersDatabaseReference.orderByChild("email").equalTo(userEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User found
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Access user data
                        User authenticatedUserFromDb = snapshot.getValue(User.class);
                        if (authenticatedUserFromDb != null){
                            if (userIsAdmin(authenticatedUserFromDb)){
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
                    }
                } else {
                    // User not found
                    Log.d("User Data", "User not found for email: " + userEmail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Error", "Failed to read user data.", databaseError.toException());
            }
        });
    }

    private void getFaculties() {
        Query query = FirebaseDatabase.getInstance().getReference("faculties");
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                faculties.clear();  // because everytime when data updates in your firebase database it creates the list with updated items
                // so to avoid duplicate fields we clear the list everytime
                if (snapshot.exists()) {
                    for (DataSnapshot facultySnapshot : snapshot.getChildren()) {
                        Faculty faculty = facultySnapshot.getValue(Faculty.class);
                        faculties.add(faculty);
                    }
                    facultyAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void deleteFaculty(Faculty facultyToDelete){
        facultiesDatabaseReference.child(facultyToDelete.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                storageReference.child("faculties/" + facultyToDelete.getId() + ".jpg").delete();
                Toast.makeText(FacultyActivity.this, R.string.delete_faculty_success, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(FacultyActivity.this, R.string.delete_faculty_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void editFaculty(String facultyId, String facultyName, String facultyDescription){

        Faculty faculty = new Faculty(facultyId, facultyName, facultyDescription);
        faculty.setChairs(facultyToUpdate.getChairs());

        if (selectedImageUri != null){
            storageReference.child("faculties/" + facultyId + ".jpg").delete();
            uploadImageToFirebaseStorage(selectedImageUri, facultyId);
            selectedImageUri = null;
        }else{
            faculty.setFacultyPosterPath(facultyToUpdate.getFacultyPosterPath());
        }

        facultiesDatabaseReference.child(facultyId).setValue(faculty).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(FacultyActivity.this, R.string.update_faculty_success, Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            } else {
                Toast.makeText(FacultyActivity.this, R.string.update_faculty_failure, Toast.LENGTH_SHORT).show();
                Log.d("FailureAddFaculty", task.getException().getMessage());
            }
        });
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
                        // Simulează un proces de reîmprospătare inițial
                        try {
                            Thread.sleep(2000); // Simulează un proces de reîmprospătare care durează 2 secunde
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Procesează datele în fundal
                        getFaculties();

                        // Actualizează UI-ul în firul principal
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Opriți indicatorul de reîmprospătare
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