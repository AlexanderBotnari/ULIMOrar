package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ulimorar.R;
import com.example.ulimorar.adapters.ChairAdapter;
import com.example.ulimorar.entities.Chair;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.utils.GetDialogsStandartButtons;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChairActivity extends AppCompatActivity {

    private FloatingActionButton addChairButton;
    private TextView titleTextView;
    private TextInputLayout chairNameTextInput;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Faculty currentFaculty;

    private List<Chair> chairsList;
    private RecyclerView chairRecyclerView;
    private ChairAdapter chairAdapter;

    private DatabaseReference facultiesDatabaseReference;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.chairs_activity_title);
        setContentView(R.layout.activity_chair);

        Intent intent = getIntent();
        currentFaculty = (Faculty) intent.getSerializableExtra("facultyFromIntent");

        chairsList = new ArrayList<>();

        facultiesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("faculties");

        addChairButton = findViewById(R.id.addChairFloatingButton);
        titleTextView = findViewById(R.id.titleTextView);
        chairRecyclerView = findViewById(R.id.chairRecycleView);
        chairAdapter = new ChairAdapter(chairsList, this, currentFaculty);
        chairRecyclerView.setHasFixedSize(true);
        chairRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chairRecyclerView.setAdapter(chairAdapter);

        if (currentFaculty != null){
            titleTextView.setText(currentFaculty.getFacultyName());
        }
        addChairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(R.string.add_chair_dialog_title);
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getChairs();
            }
        });

    }

    private void openDialog(int dialogTitle) {
        View alertDialogCustomView = LayoutInflater.from(this).inflate(R.layout.add_edit_chair_dialog, null);

        TextView titleTextView = alertDialogCustomView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(dialogTitle);
        chairNameTextInput = alertDialogCustomView.findViewById(R.id.chairNameTextInput);
        TextInputEditText chairNameEditText = (TextInputEditText) chairNameTextInput.getEditText();

        // Construiți interfața de dialog personalizată
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

        // Afișați dialogul
        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        GetDialogsStandartButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewChairToFaculty(chairNameEditText.getText().toString());
            }
        });

        GetDialogsStandartButtons.getCancelButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    private void addNewChairToFaculty(String chairName) {

        boolean isValid = true;

        if (chairName.isEmpty()){
            chairNameTextInput.setError(getText(R.string.empty_chair_name_error));
            isValid = false;
        }else {
            chairNameTextInput.setError(null);
        }

        if (isValid){
            Chair chair = new Chair(chairName, String.valueOf(chairName.charAt(0)));
            chairsList.add(chair);

            currentFaculty.setChairs(chairsList);

            facultiesDatabaseReference.child(currentFaculty.getId()).setValue(currentFaculty).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    Toast.makeText(ChairActivity.this, R.string.add_chair_successful_message, Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(ChairActivity.this, R.string.failure_add_chair_error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        getChairs();
    }

    private void getChairs() {
        Query query = FirebaseDatabase.getInstance().getReference("faculties").child(currentFaculty.getId()).child("chairs");
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                chairsList.clear();  // because everytime when data updates in your firebase database it creates the list with updated items
                // so to avoid duplicate fields we clear the list everytime
                if (snapshot.exists()) {
                    for (DataSnapshot chairSnapshot : snapshot.getChildren()) {
                        Chair chair = chairSnapshot.getValue(Chair.class);
                        chairsList.add(chair);
                    }
                    chairAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.faculty:
                startActivity(new Intent(ChairActivity.this, FacultyActivity.class));
                return true;
            case R.id.users:
                startActivity(new Intent(ChairActivity.this, UsersActivity.class));
                return true;
            case R.id.logout:
                Toast.makeText(this, R.string.logout_message, Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChairActivity.this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}