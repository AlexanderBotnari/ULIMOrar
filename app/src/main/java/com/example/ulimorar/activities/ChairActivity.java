package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;

public class ChairActivity extends AppCompatActivity implements BottomSheetListener {

    private FloatingActionButton addChairButton;
    private TextView titleTextView;
    private ImageView emptyImageView;
    private TextInputLayout chairNameTextInput;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SwipeController swipeController;

    private Faculty currentFaculty;
    private boolean isAdmin;
    private String authenticatedUserEmail;

    private Map<String, Chair> chairsList;
    private RecyclerView chairRecyclerView;
    private ChairAdapter chairAdapter;

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

        chairsList = new HashMap<>();

        chairViewModel = new ViewModelProvider(this).get(ChairViewModel.class);

        addChairButton = findViewById(R.id.addChairFloatingButton);

        titleTextView = findViewById(R.id.titleTextView);
        emptyImageView = findViewById(R.id.emptyImageView);

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

        chairViewModel.getChairsListLiveData().observe(this, new Observer<Map<String, Chair>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(Map<String, Chair> chairs) {
                if (!chairs.isEmpty()){
                    emptyImageView.setVisibility(View.GONE);
                    chairAdapter.setChairs(chairs);
                    chairAdapter.notifyDataSetChanged();

                    swipeRefreshLayout.setRefreshing(false);
                }else {
                    emptyImageView.setVisibility(View.VISIBLE);
                }
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        if (!isAddDialog){
            List<String> keys = new ArrayList<>(chairAdapter.getChairs().keySet());
            String chairId = keys.get(itemPosition);
            chairToUpdate = chairAdapter.getChairs().get(chairId);
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
                        chairViewModel.addNewChairToFaculty(currentFaculty, chairName,
                                ChairActivity.this, alertDialog);
                    }else{
                        chairViewModel.editChair(currentFaculty, chairToUpdate, chairName,
                                ChairActivity.this, alertDialog);
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
        List<String> keys = new ArrayList<>(currentFaculty.getChairs().keySet());
        String chairId = keys.get(chairPositionToDelete);
        chairViewModel.deleteChair(currentFaculty, chairId,ChairActivity.this, bottomSheetFragment);
    }
}