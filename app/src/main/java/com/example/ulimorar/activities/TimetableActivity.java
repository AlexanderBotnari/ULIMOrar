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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ulimorar.R;
import com.example.ulimorar.adapters.TimetableAdapter;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.entities.Group;
import com.example.ulimorar.entities.Timetable;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.example.ulimorar.viewmodels.TimetableViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class TimetableActivity extends AppCompatActivity{

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    private FloatingActionButton addTimetableButton;
    private ImageButton addTimetableImageButton;
    private ImageView emptyImageView;
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

    private TimetableViewModel timetableViewModel;

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

        timetableViewModel = new ViewModelProvider(this).get(TimetableViewModel.class);

        recyclerView = findViewById(R.id.timeTableActivityRecyclerView);
        timetableAdapter = new TimetableAdapter(timetableList, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(timetableAdapter);
        timetableAdapter.setAdmin(true);

        emptyImageView = findViewById(R.id.emptyImageView);
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
                timetableViewModel.getTimetables(currentFaculty, chairIndex, groupIndex);
            }
        });

        timetableViewModel.getTimetableListLiveData().observe(this, new Observer<List<Timetable>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<Timetable> timetables) {
                if (!timetables.isEmpty()){
                    emptyImageView.setVisibility(View.GONE);
                    timetableAdapter.setTimetables(timetables);
                    timetableAdapter.notifyDataSetChanged();

                    swipeRefreshLayout.setRefreshing(false);
                }else {
                    emptyImageView.setVisibility(View.VISIBLE);
                }
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

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
                        timetableViewModel.addNewTimetableToGroup(currentFaculty, currentGroup, timetableName,
                                chairIndex, groupIndex, TimetableActivity.this, alertDialog, selectedImageUri);
                    }else{
                        timetableViewModel.editTimetable(itemPosition, timetableName, selectedImageUri,
                                timetableToUpdate, currentGroup, currentFaculty, chairIndex, groupIndex,
                                TimetableActivity.this, alertDialog);
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

    public Group getCurrentGroup() {
        return currentGroup;
    }

    public Faculty getCurrentFaculty() {
        return currentFaculty;
    }

    public String getChairIndex() {
        return chairIndex;
    }

    public String getGroupIndex() {
        return groupIndex;
    }

    @Override
    protected void onStart() {
        super.onStart();
        timetableViewModel.getTimetables(currentFaculty, chairIndex, groupIndex);
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