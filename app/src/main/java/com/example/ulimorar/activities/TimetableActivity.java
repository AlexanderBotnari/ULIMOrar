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
import com.example.ulimorar.adapters.TimetableAdapter;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.entities.Group;
import com.example.ulimorar.entities.Timetable;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimetableActivity extends AppCompatActivity{

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    private FloatingActionButton addTimetableButton;
    private ImageButton addTimetableImageButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputLayout timetableNameTextInput;

    private Group currentGroup;
    private Faculty currentFaculty;
    private String chairIndex;
    private String groupIndex;
    private boolean isAdmin;
    private String authenticatedUserEmail;

    private List<Timetable> timetableList;
    private RecyclerView recyclerView;
    private TimetableAdapter timetableAdapter;

    private AlertDialog alertDialog;

    private DatabaseReference groupsDatabaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private Timetable timetableToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        currentGroup = (Group) intent.getSerializableExtra("groupFromIntent");
        currentFaculty = (Faculty) intent.getSerializableExtra("currentFaculty");
        chairIndex = intent.getStringExtra("chairIndex");
        groupIndex = intent.getStringExtra("groupIndex");
        isAdmin = intent.getBooleanExtra("userIsAdmin", false);
        authenticatedUserEmail = intent.getStringExtra("currentUserEmail");

        setTitle(currentGroup.getGroupName());
        setContentView(R.layout.activity_timetable);

        timetableList = new ArrayList<>();

        groupsDatabaseReference = FirebaseDatabase.getInstance().getReference("faculties")
                .child(currentFaculty.getId()).child("chairs")
                .child(chairIndex).child("groups");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        recyclerView = findViewById(R.id.timeTableActivityRecyclerView);
        timetableAdapter = new TimetableAdapter(timetableList, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(timetableAdapter);
        timetableAdapter.setAdmin(true);

        addTimetableButton = findViewById(R.id.addTimetableFloatingButton);
        if (!isAdmin){
            addTimetableButton.setVisibility(View.GONE);
            timetableAdapter.setAdmin(false);
        }
        addTimetableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(R.string.add_timetable_dialog_title, true, null);
            }
        });

        // Initialize the ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        addTimetableImageButton.setImageResource(R.drawable.ic_succes);
                        Intent data = result.getData();
                        if (data != null) {
                            selectedImageUri = data.getData();
                        }
                    }else{
                        addTimetableImageButton.setImageResource(R.drawable.ic_failed);
                    }
                });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTimetables();
            }
        });

    }

    public void openDialog(int dialogTitle, boolean isAddDialog, Integer itemPosition) {
        View alertDialogCustomView = LayoutInflater.from(this).inflate(R.layout.add_edit_timetable_dialog, null);

        TextView titleTextView = alertDialogCustomView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(dialogTitle);
        timetableNameTextInput = alertDialogCustomView.findViewById(R.id.timetableNameTextInput);
        TextInputEditText timetableNameEditText = (TextInputEditText) timetableNameTextInput.getEditText();
        addTimetableImageButton = alertDialogCustomView.findViewById(R.id.timetableAddImageButton);

        addTimetableImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                imagePickerLauncher.launch(intent);
            }
        });

        // Construiți interfața de dialog personalizată
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

        // Afișați dialogul
        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        if (!isAddDialog){
            timetableToUpdate = timetableAdapter.getTimetables().get(itemPosition);
            timetableNameTextInput.getEditText().setText(timetableToUpdate.getTimetableName());
        }

        GetDialogsStandardButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timetableName = timetableNameEditText.getText().toString();

                boolean isValid = true;

                if (timetableName.isEmpty()){
                    timetableNameTextInput.setError(getText(R.string.empty_timetable_name_error));
                    isValid = false;
                }else{
                    timetableNameTextInput.setError(null);
                }

                if (isValid){
                    if (isAddDialog){
                        addNewTimetableToGroup(timetableName);
                    }else{
                        editTimetable(itemPosition, timetableName);
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

    private void uploadImageToFirebaseStorage(Uri imageUri, String sessionName, Long date, String timetableIndex) {
        if (imageUri != null) {
            // Create a reference to "images/[filename]"
            StorageReference imageFacultyRef = storageReference.child("timetables/" + currentGroup.getGroupName() + "-" + sessionName + "-" + date + ".jpg");

            // Upload the file to Firebase Storage
            imageFacultyRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image uploaded successfully
                            Toast.makeText(TimetableActivity.this, R.string.image_uploaded_successful_message, Toast.LENGTH_SHORT).show();

                            // Get the download URL and update the faculty by id in realtime database
                            imageFacultyRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUrl) {
                                    Log.d("DownloadUrl", downloadUrl.toString());
                                    groupsDatabaseReference.child(groupIndex).child("timetables").child(timetableIndex).child("imageUrl").setValue(downloadUrl.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            // Handle unsuccessful uploads
                            Toast.makeText(TimetableActivity.this, R.string.failure_image_upload, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void getTimetables() {
        Query query = FirebaseDatabase.getInstance().getReference("faculties").child(currentFaculty.getId())
                .child("chairs").child(chairIndex).child("groups").child(groupIndex).child("timetables");
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                timetableList.clear();  // because everytime when data updates in your firebase database it creates the list with updated items
                // so to avoid duplicate fields we clear the list everytime
                if (snapshot.exists()) {
                    for (DataSnapshot timetableSnapshot : snapshot.getChildren()) {
                        Timetable timetable = timetableSnapshot.getValue(Timetable.class);
                        timetableList.add(timetable);
                    }
                    timetableAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void addNewTimetableToGroup(String timetableName) {
            Timetable timetable = new Timetable(timetableName, new Date().getTime());

            timetableList.add(timetable);
            currentGroup.setTimetables(timetableList);

            groupsDatabaseReference.child(groupIndex).setValue(currentGroup).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            Toast.makeText(TimetableActivity.this, R.string.add_timetable_successful_message, Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Toast.makeText(TimetableActivity.this, R.string.failure_add_timetable_error, Toast.LENGTH_SHORT).show();
                            Log.d("FailureAddTimetable", e.getMessage());
                        }
                    });

            // Upload the selected image to Firebase Storage or perform other actions
            uploadImageToFirebaseStorage(selectedImageUri, timetable.getTimetableName(), timetable.getUpdateTime(), String.valueOf(timetableList.indexOf(timetable)));
            selectedImageUri = null;
    }

    public void editTimetable(Integer itemPosition, String timetableName){
        Timetable newTimetable = new Timetable(timetableName,  new Date().getTime());

        if (selectedImageUri != null){
            storageReference.child("timetables/" + currentGroup.getGroupName() + "-" +
                    timetableToUpdate.getTimetableName() + "-" + timetableToUpdate.getUpdateTime() + ".jpg").delete();
            uploadImageToFirebaseStorage(selectedImageUri, newTimetable.getTimetableName(), newTimetable.getUpdateTime(), String.valueOf(itemPosition));
            selectedImageUri = null;
        }else{
            newTimetable.setImageUrl(timetableToUpdate.getImageUrl());
        }

        groupsDatabaseReference.child(groupIndex).child("timetables").child(String.valueOf(itemPosition))
                .setValue(newTimetable).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                Toast.makeText(TimetableActivity.this, R.string.update_timetable_success, Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(TimetableActivity.this, R.string.update_timetable_failure, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteTimetable(String timetablePosition ,Timetable timetableToDelete){
        groupsDatabaseReference.child(groupIndex).child("timetables").child(timetablePosition).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                storageReference.child("timetables/" + currentGroup.getGroupName() + "-" +
                        timetableToDelete.getTimetableName() + "-" + timetableToDelete.getUpdateTime() + ".jpg").delete();
                Toast.makeText(TimetableActivity.this, R.string.delete_timetable_success, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(TimetableActivity.this, R.string.delete_timetable_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getTimetables();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.users).setVisible(isAdmin);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home: finish();
                return true;
            case R.id.users:
                startActivity(new Intent(TimetableActivity.this, UsersActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            case R.id.faculty:
                startActivity(new Intent(TimetableActivity.this, FacultyActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            case R.id.account:
                startActivity(new Intent(TimetableActivity.this, AccountActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}