package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ulimorar.R;
import com.example.ulimorar.adapters.GroupAdapter;
import com.example.ulimorar.entities.Chair;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.entities.Group;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.fragments.interfaces.BottomSheetListener;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.example.ulimorar.utils.controllers.SwipeController;
import com.example.ulimorar.utils.controllers.SwipeControllerActions;
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

public class GroupActivity extends AppCompatActivity implements BottomSheetListener {

    private FloatingActionButton addGroupButton;
    private TextView titleTextView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputLayout groupNameTextInput;
    private TextInputLayout groupSymbolTextInput;
    private SwipeController swipeController;

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

    private Group groupToUpdate;
    private int groupPositionToDelete;

    private DeleteBottomSheetFragment bottomSheetFragment;

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

        setupRecyclerView();

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
                openDialog(R.string.add_group_dialog_title, true, null);
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

    private void setupRecyclerView(){
        recyclerView = findViewById(R.id.groupRecycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter = new GroupAdapter(groupList, this, currentFaculty, currentChair, currentChairKey);
        recyclerView.setAdapter(groupAdapter);
        groupAdapter.setAdmin(true);

        if (isAdmin){
            swipeController = new SwipeController(new SwipeControllerActions() {
                @Override
                public void onRightClicked (int position) {
                    groupPositionToDelete = position;
                    bottomSheetFragment = new DeleteBottomSheetFragment();
                    bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                    bottomSheetFragment.setBottomSheetListener(GroupActivity.this);
                }

                @Override
                public void onLeftClicked(int position) {
                    openDialog(R.string.edit_group_dialog_title, false, position);
                }
            });
            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
            itemTouchhelper.attachToRecyclerView(recyclerView);

            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                    swipeController.onDraw(c);
                }
            });
        }
    }

    private void openDialog(int dialogTitle, boolean isAddDialog, Integer itemPosition) {
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

        if (!isAddDialog){
            groupToUpdate = groupAdapter.getGroups().get(itemPosition);
            groupNameTextInput.getEditText().setText(groupToUpdate.getGroupName());
            groupSymbolTextInput.getEditText().setText(groupToUpdate.getGroupSymbol());
        }

        GetDialogsStandardButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupName = groupNameEditText.getText().toString();
                String groupSymbol = groupSymbolEditText.getText().toString();

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

                if (isValid) {
                    if (groupSymbol.length() <= 3) {
                        if (isAddDialog){
                            addNewGroupToChair(groupName, groupSymbol);
                        }else {
                            editGroup(itemPosition, groupName, groupSymbol);
                        }
                    }else {
                        groupSymbolTextInput.setError(getText(R.string.group_symbol_max_length));
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

    private void addNewGroupToChair(String groupName, String groupSymbol) {
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

    public void editGroup(int groupPositionToUpdate, String groupName, String groupSymbol){
        Group newGroup = new Group(groupName, groupSymbol);
        newGroup.setTimetables(groupToUpdate.getTimetables());

        chairsDatabaseReference.child(currentChairKey).child("groups").
                child(String.valueOf(groupPositionToUpdate)).setValue(newGroup).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(GroupActivity.this, R.string.update_group_success, Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(GroupActivity.this, R.string.update_group_success, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteGroup(int groupPositionToDelete){
        chairsDatabaseReference.child(currentChairKey).child("groups")
                .child(String.valueOf(groupPositionToDelete)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(GroupActivity.this, R.string.delete_group_success, Toast.LENGTH_SHORT).show();
                        bottomSheetFragment.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(GroupActivity.this, R.string.delete_group_failure, Toast.LENGTH_SHORT).show();
                        bottomSheetFragment.dismiss();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupAdapter.setAuthenticatedUserEmail(authenticatedUserEmail);
        getGroups();
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
                startActivity(new Intent(GroupActivity.this, UsersActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            case R.id.faculty:
                startActivity(new Intent(GroupActivity.this, FacultyActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            case R.id.logout:
                Toast.makeText(this, R.string.logout_message, Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(GroupActivity.this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onButtonCancel() {
        bottomSheetFragment.dismiss();
    }

    @Override
    public void onButtonDelete() {
        deleteGroup(groupPositionToDelete);
    }
}