package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ulimorar.R;
import com.example.ulimorar.adapters.ChairAdapter;
import com.example.ulimorar.entities.Chair;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.fragments.interfaces.BottomSheetListener;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.example.ulimorar.utils.controllers.SwipeController;
import com.example.ulimorar.utils.controllers.SwipeControllerActions;
import com.example.ulimorar.viewmodels.ChairViewModel;
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

public class ChairActivity extends AppCompatActivity implements BottomSheetListener {

    private FloatingActionButton addChairButton;
    private TextView titleTextView;
    private TextInputLayout chairNameTextInput;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeController swipeController;

    private Faculty currentFaculty;
    private boolean isAdmin;
    private String authenticatedUserEmail;

    private List<Chair> chairsList;
    private RecyclerView chairRecyclerView;
    private ChairAdapter chairAdapter;

//    private DatabaseReference facultiesDatabaseReference;
    private ChairViewModel chairViewModel;

    private AlertDialog alertDialog;

    private Chair chairToUpdate;
    private int chairPositionToDelete;

    private DeleteBottomSheetFragment bottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.chairs_activity_title);
        setContentView(R.layout.activity_chair);

        Intent intent = getIntent();
        currentFaculty = (Faculty) intent.getSerializableExtra("facultyFromIntent");
        isAdmin = intent.getBooleanExtra("userIsAdmin", false);
        authenticatedUserEmail = intent.getStringExtra("currentUserEmail");

        chairsList = new ArrayList<>();

//        facultiesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("faculties");
        chairViewModel = new ViewModelProvider(this).get(ChairViewModel.class);

        addChairButton = findViewById(R.id.addChairFloatingButton);

        titleTextView = findViewById(R.id.titleTextView);

        setupRecyclerView();

        if (currentFaculty != null){
            titleTextView.setText(currentFaculty.getFacultyName());
        }

        if (!isAdmin){
            addChairButton.setVisibility(View.GONE);
            chairAdapter.setAdmin(false);
        }

        addChairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(R.string.add_chair_dialog_title, true, null);
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chairViewModel.getChairs(currentFaculty);
            }
        });

    }

    private void setupRecyclerView(){
        chairRecyclerView = findViewById(R.id.chairRecycleView);
        chairAdapter = new ChairAdapter(chairsList, this, currentFaculty);
        chairRecyclerView.setHasFixedSize(true);
        chairRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chairRecyclerView.setAdapter(chairAdapter);
        chairAdapter.setAdmin(true);

        chairViewModel.getChairsListLiveData().observe(this, new Observer<List<Chair>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<Chair> chairs) {
                chairAdapter.setChairs(chairs);
                chairAdapter.notifyDataSetChanged();

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        if (isAdmin){
            swipeController = new SwipeController(new SwipeControllerActions() {
                @Override
                public void onRightClicked (int position) {
                    chairPositionToDelete = position;
                    bottomSheetFragment = new DeleteBottomSheetFragment();
                    bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                    bottomSheetFragment.setBottomSheetListener(ChairActivity.this);
                }

                @Override
                public void onLeftClicked(int position) {
                    openDialog(R.string.edit_chair_dialog_title, false, position);
                }

            });
            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
            itemTouchhelper.attachToRecyclerView(chairRecyclerView);

            chairRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                    swipeController.onDraw(c);
                }
            });
        }

    }

    private void openDialog(int dialogTitle, boolean isAddDialog, Integer itemPosition) {
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

        if (!isAddDialog){
            chairToUpdate = chairAdapter.getChairs().get(itemPosition);
            chairNameTextInput.getEditText().setText(chairToUpdate.getChairName());
        }

        GetDialogsStandardButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String chairName = chairNameEditText.getText().toString();

                boolean isValid = true;

                if (chairName.isEmpty()){
                    chairNameTextInput.setError(getText(R.string.empty_chair_name_error));
                    isValid = false;
                }else {
                    chairNameTextInput.setError(null);
                }

                if (isValid){
                    if (isAddDialog){
//                        addNewChairToFaculty(chairName);
                        chairViewModel.addNewChairToFaculty(currentFaculty, chairName,
                                ChairActivity.this, alertDialog);
                    }else{
//                        editChair(itemPosition, chairName);
                        chairViewModel.editChair(currentFaculty, chairToUpdate, itemPosition,
                                chairName, ChairActivity.this, alertDialog);
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

//    private void addNewChairToFaculty(String chairName) {
//            Chair chair = new Chair(chairName, String.valueOf(chairName.charAt(0)));
//            chairsList.add(chair);
//
//            currentFaculty.setChairs(chairsList);
//
//            facultiesDatabaseReference.child(currentFaculty.getId()).setValue(currentFaculty).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull @NotNull Task<Void> task) {
//                    Toast.makeText(ChairActivity.this, R.string.add_chair_successful_message, Toast.LENGTH_SHORT).show();
//                    alertDialog.dismiss();
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull @NotNull Exception e) {
//                    Toast.makeText(ChairActivity.this, R.string.failure_add_chair_error, Toast.LENGTH_LONG).show();
//                }
//            });
//    }

//    public void deleteChair(int chairPositionToDelete){
//        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs").
//                child(String.valueOf(chairPositionToDelete)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull @NotNull Task<Void> task) {
//                Toast.makeText(ChairActivity.this, R.string.delete_chair_success, Toast.LENGTH_SHORT).show();
//                bottomSheetFragment.dismiss();
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull @NotNull Exception e) {
//                Toast.makeText(ChairActivity.this, R.string.delete_chair_failure, Toast.LENGTH_SHORT).show();
//                bottomSheetFragment.dismiss();
//            }
//        });
//    }

//    public void editChair(int chairPositionToUpdate, String chairName){
//        Chair newChair = new Chair(chairName, String.valueOf(chairName.charAt(0)));
//        newChair.setGroups(chairToUpdate.getGroups());
//
//        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs").
//                child(String.valueOf(chairPositionToUpdate)).setValue(newChair).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull @NotNull Task<Void> task) {
//                        Toast.makeText(ChairActivity.this, R.string.update_chair_success, Toast.LENGTH_SHORT).show();
//                        alertDialog.dismiss();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull @NotNull Exception e) {
//                        Toast.makeText(ChairActivity.this, R.string.update_chair_failure, Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

//    private void getChairs() {
//        Query query = FirebaseDatabase.getInstance().getReference("faculties").child(currentFaculty.getId()).child("chairs");
//        query.addValueEventListener(new ValueEventListener() {
//            @SuppressLint("NotifyDataSetChanged")
//            @Override
//            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
//                chairsList.clear();  // because everytime when data updates in your firebase database it creates the list with updated items
//                // so to avoid duplicate fields we clear the list everytime
//                if (snapshot.exists()) {
//                    for (DataSnapshot chairSnapshot : snapshot.getChildren()) {
//                        Chair chair = chairSnapshot.getValue(Chair.class);
//                        chairsList.add(chair);
//                    }
//                    chairAdapter.notifyDataSetChanged();
//                    swipeRefreshLayout.setRefreshing(false);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull @NotNull DatabaseError error) {
//
//            }
//        });
//    }

    @Override
    protected void onStart() {
        super.onStart();
        chairAdapter.setAuthenticatedUserEmail(authenticatedUserEmail);
        chairViewModel.getChairs(currentFaculty);
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
                startActivity(new Intent(ChairActivity.this, UsersActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            case R.id.faculty:
                startActivity(new Intent(ChairActivity.this, FacultyActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            case R.id.account:
                startActivity(new Intent(ChairActivity.this, AccountActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
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
    public void onButtonDelete(View view) {
//        deleteChair(chairPositionToDelete);
        chairViewModel.deleteChair(currentFaculty, chairPositionToDelete, ChairActivity.this, bottomSheetFragment);
    }
}