package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ulimorar.R;
import com.example.ulimorar.adapters.GroupAdapter;
import com.example.ulimorar.entities.Chair;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.entities.Group;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private FloatingActionButton addGroupButton;
    private TextView titleTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputLayout groupNameTextInput;
    private TextInputLayout groupSymbolTextInput;

    private Chair currentChair;
    private Faculty currentFaculty;

    private List<Group> groupList;
    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;

    private DatabaseReference chairsDatabaseReference;

    private String currentChairKey;
    private boolean isAdmin;
    private String authenticatedUserEmail;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.groups_activity_title);
        setContentView(R.layout.activity_group);

        Intent intent = getIntent();
        currentChair = (Chair) intent.getSerializableExtra("chairFromIntent");
        currentChairKey = intent.getStringExtra("chairIndex");
        currentFaculty = (Faculty) intent.getSerializableExtra("currentFaculty");
        isAdmin = intent.getBooleanExtra("userIsAdmin", false);
        authenticatedUserEmail = intent.getStringExtra("currentUserEmail");

        groupList = new ArrayList<>();

        chairsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("faculties").child(currentFaculty.getId()).child("chairs");

        titleTextView = findViewById(R.id.titleTextView);
        addGroupButton = findViewById(R.id.addGroupFloatingButton);
        recyclerView = findViewById(R.id.groupRecycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter = new GroupAdapter(groupList, this, currentFaculty, currentChair, currentChairKey);
        recyclerView.setAdapter(groupAdapter);
        groupAdapter.setAdmin(true);

        if (currentChair != null){
            titleTextView.setText(currentChair.getChairName());
        }

        if (!isAdmin){
            addGroupButton.setVisibility(View.GONE);
            groupAdapter.setAdmin(false);
        }

        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(R.string.add_group_dialog_title);
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getGroups();
            }
        });

    }

    private void openDialog(int dialogTitle) {
        View alertDialogCustomView = LayoutInflater.from(this).inflate(R.layout.add_edit_group_dialog, null);

        TextView titleTextView = alertDialogCustomView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(dialogTitle);
        groupNameTextInput = alertDialogCustomView.findViewById(R.id.groupNameTextInput);
        groupSymbolTextInput = alertDialogCustomView.findViewById(R.id.groupSymbolTextInput);
        TextInputEditText groupNameEditText = (TextInputEditText) groupNameTextInput.getEditText();
        TextInputEditText groupSymbolEditText = (TextInputEditText) groupSymbolTextInput.getEditText();

        // Construiți interfața de dialog personalizată
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

        // Afișați dialogul
        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        GetDialogsStandardButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewGroupToChair(groupNameEditText.getText().toString(),
                                    groupSymbolEditText.getText().toString());
            }
        });

        GetDialogsStandardButtons.getCancelButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    private void addNewGroupToChair(String groupName, String groupSymbol) {

        boolean isValid = true;

        if (groupName.isEmpty()){
            groupNameTextInput.setError(getText(R.string.empty_group_name_error));
            isValid = false;
        }else {
            groupNameTextInput.setError(null);
        }

        if (groupSymbol.isEmpty()){
            groupSymbolTextInput.setError(getText(R.string.empty_group_symbol_error));
            isValid = false;
        }else {
            groupSymbolTextInput.setError(null);
        }

        if (isValid){
            if (groupSymbol.length() <= 3) {
                Group group = new Group(groupName, groupSymbol);
                groupList.add(group);

                currentChair.setGroups(groupList);

                if (currentChairKey != null){
                    chairsDatabaseReference.child(currentChairKey).setValue(currentChair).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            Toast.makeText(GroupActivity.this, R.string.add_group_successful_message, Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Toast.makeText(GroupActivity.this, R.string.failure_add_group_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupAdapter.setAuthenticatedUserEmail(authenticatedUserEmail);
        getGroups();
    }

    private void getGroups() {
        if (currentChairKey != null){
            Query query = FirebaseDatabase.getInstance().getReference("faculties").child(currentFaculty.getId())
                    .child("chairs").child(String.valueOf(currentChairKey)).child("groups");
            query.addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    groupList.clear();  // because everytime when data updates in your firebase database it creates the list with updated items
                    // so to avoid duplicate fields we clear the list everytime
                    if (snapshot.exists()) {
                        for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                            Group group = groupSnapshot.getValue(Group.class);
                            groupList.add(group);
                        }
                        groupAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            startActivity(new Intent(GroupActivity.this, FacultyActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}