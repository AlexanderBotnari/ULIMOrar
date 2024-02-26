package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.example.ulimorar.adapters.GroupAdapter;
import com.example.ulimorar.entities.Chair;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.entities.Group;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.fragments.interfaces.BottomSheetListener;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.example.ulimorar.utils.controllers.SwipeController;
import com.example.ulimorar.utils.controllers.SwipeControllerActions;
import com.example.ulimorar.viewmodels.GroupViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupActivity extends AppCompatActivity implements BottomSheetListener {

    private FloatingActionButton addGroupButton;
    private TextView titleTextView;
    private ImageView emptyImageView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputLayout groupNameTextInput;
    private TextInputLayout groupSymbolTextInput;
    private SwipeController swipeController;

    private Chair currentChair;
    private Faculty currentFaculty;

    private Map<String, Group> groupList;
    private RecyclerView recyclerView;
    private GroupAdapter groupAdapter;

    private GroupViewModel groupViewModel;

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
        currentFaculty = (Faculty) intent.getSerializableExtra("currentFaculty");
        isAdmin = intent.getBooleanExtra("userIsAdmin", false);
        authenticatedUserEmail = intent.getStringExtra("currentUserEmail");

        groupList = new HashMap<>();

        groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        emptyImageView = findViewById(R.id.emptyImageView);
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
                groupViewModel.getGroups(currentChair.getId(), currentFaculty);
            }
        });

    }

    private void setupRecyclerView(){
        recyclerView = findViewById(R.id.groupRecycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupAdapter = new GroupAdapter(groupList, this, currentFaculty, currentChair);
        recyclerView.setAdapter(groupAdapter);
        groupAdapter.setAdmin(true);

        groupViewModel.getGroupListLiveData().observe(this, new Observer<Map<String, Group>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(Map<String, Group> groups) {
                if (!groups.isEmpty()){
                    emptyImageView.setVisibility(View.GONE);
                    groupAdapter.setGroups(groups);
                    groupAdapter.notifyDataSetChanged();

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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        if (!isAddDialog){
            List<String> keys = new ArrayList<>(groupAdapter.getGroups().keySet());
            String groupId = keys.get(itemPosition);
            groupToUpdate = groupAdapter.getGroups().get(groupId);
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
                            groupViewModel.addNewGroupToChair(currentChair, currentFaculty,
                                    groupName, groupSymbol, GroupActivity.this, alertDialog);
                        }else {
                            groupViewModel.editGroup(groupToUpdate, currentFaculty, currentChair.getId(),
                                     groupName, groupSymbol, GroupActivity.this, alertDialog);
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

    @Override
    protected void onStart() {
        super.onStart();
        groupAdapter.setAuthenticatedUserEmail(authenticatedUserEmail);
        groupViewModel.getGroups(currentChair.getId(), currentFaculty);
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
            case R.id.account:
                startActivity(new Intent(GroupActivity.this, AccountActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
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
        List<String> keys = new ArrayList<>(currentChair.getGroups().keySet());
        String groupId = keys.get(groupPositionToDelete);

        groupViewModel.deleteGroup(currentFaculty, currentChair.getId(), groupId,
                GroupActivity.this, bottomSheetFragment);
    }
}