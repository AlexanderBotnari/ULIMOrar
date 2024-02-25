package com.example.ulimorar.activities.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.ulimorar.R;
import com.example.ulimorar.adapters.UserAdapter;
import com.example.ulimorar.callbacks.EmailExistCallback;
import com.example.ulimorar.callbacks.PassportExistCallback;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.fragments.interfaces.BottomSheetListener;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.example.ulimorar.utils.controllers.SwipeController;
import com.example.ulimorar.utils.controllers.SwipeControllerActions;
import com.example.ulimorar.viewmodels.UserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class RegisteredUsersFragment extends Fragment implements BottomSheetListener {

    private AlertDialog alertDialog;

    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputLayout firstNameInputLayout;
    private TextInputLayout lastNameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout idnpInputLayout;
    private FloatingActionButton addUserButton;
    private ImageView emptyImageView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Spinner roleSpinner;

    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<User> users;

    private SwipeController swipeController;

    private User userToUpdate;
    private DeleteBottomSheetFragment bottomSheetFragment;
    private User userToDelete;

    private SearchView searchView;
    private List<User> searchResults;

    private UserViewModel userViewModel;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registered_users, container, false);

        addUserButton = view.findViewById(R.id.addUserFloatingButton);
        searchView = view.findViewById(R.id.searchView);

        setupRecyclerView(view);

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(R.string.add_user_dialog_title, true, null, view);
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                userViewModel.getUsers();
                searchView.clearFocus();
                searchView.setQueryHint(getText(R.string.search));
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                searchView.setQueryHint(null);
            }
        });

        int closeBtnId = searchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
        searchView.findViewById(closeBtnId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegisteredUsersFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newQuery) {
                updateSearchResults(newQuery);
                return true;
            }
        });

        emptyImageView = view.findViewById(R.id.emptyImageView);
        userViewModel.getUsersLiveData().observe(getViewLifecycleOwner(),  userList -> {
            if (!userList.isEmpty()){
                emptyImageView.setVisibility(View.GONE);
                userAdapter.setUsers(userList);
                userAdapter.notifyDataSetChanged();

                // Stop the swipe-to-refresh animation
                swipeRefreshLayout.setRefreshing(false);
            }else {
                emptyImageView.setVisibility(View.VISIBLE);
            }

        });

        return view;
    }

    private void setupRecyclerView(View view) {
        userRecyclerView = view.findViewById(R.id.usersRecyclerView);
        users = new ArrayList<>();
        searchResults = new ArrayList<>();
        layoutManager = new LinearLayoutManager(view.getContext());
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.setLayoutManager(layoutManager);
        userAdapter = new UserAdapter(users);
        userRecyclerView.setAdapter(userAdapter);

        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked (int position){
                userToDelete = userAdapter.getUsers().get(position);
                bottomSheetFragment = new DeleteBottomSheetFragment();
                bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
                bottomSheetFragment.setBottomSheetListener(RegisteredUsersFragment.this);
            }

            @Override
            public void onLeftClicked(int position) {
                openDialog(R.string.edit_user_dialog_title, false, position, view);
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(userRecyclerView);

        userRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    private void openDialog(int dialogTitle, boolean isAddDialog, Integer itemPosition, View view) {

        View alertDialogCustomView = LayoutInflater.from(view.getContext()).inflate(R.layout.add_edit_user_dialog, null);

        TextView titleTextView = alertDialogCustomView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(dialogTitle);
        roleSpinner = alertDialogCustomView.findViewById(R.id.roleSpinner);
        firstNameInputLayout = alertDialogCustomView.findViewById(R.id.firstNameTextField);
        lastNameInputLayout = alertDialogCustomView.findViewById(R.id.lastNameTextField);
        emailInputLayout = alertDialogCustomView.findViewById(R.id.emailTextField);
        idnpInputLayout = alertDialogCustomView.findViewById(R.id.idnpTextField);
        passwordInputLayout = alertDialogCustomView.findViewById(R.id.passwordTextField);
        confirmPasswordInputLayout = alertDialogCustomView.findViewById(R.id.confirmPasswordTextField);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                view.getContext(),
                R.array.roleSpinnerArray,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        roleSpinner.setAdapter(adapter);

        // Personalized dialog interface
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setView(alertDialogCustomView);

        // Show dialog
        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        if (!isAddDialog){
            emailInputLayout.setVisibility(View.GONE);
            idnpInputLayout.setVisibility(View.GONE);
            userToUpdate = userAdapter.getUsers().get(itemPosition);
            firstNameInputLayout.getEditText().setText(userToUpdate.getFirstName());
            lastNameInputLayout.getEditText().setText(userToUpdate.getLastName());
            emailInputLayout.getEditText().setText(userToUpdate.getEmail());
            idnpInputLayout.getEditText().setText(userToUpdate.getIdnp());
            passwordInputLayout.getEditText().setText(userToUpdate.getPassword());
            confirmPasswordInputLayout.getEditText().setText(userToUpdate.getPassword());
        }

        GetDialogsStandardButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = firstNameInputLayout.getEditText().getText().toString();
                String lastName = lastNameInputLayout.getEditText().getText().toString();
                String email = emailInputLayout.getEditText().getText().toString();
                String idnp = idnpInputLayout.getEditText().getText().toString();
                String role = roleSpinner.getSelectedItem().toString();
                String password = passwordInputLayout.getEditText().getText().toString();
                String confirmPassword = confirmPasswordInputLayout.getEditText().getText().toString();

                boolean isValid = true;

                if (firstName.equals("")) {
                    firstNameInputLayout.setError(getString(R.string.first_name_is_empty));
                    isValid = false;
                }else{
                    firstNameInputLayout.setError(null);
                }

                if (lastName.equals("")) {
                    lastNameInputLayout.setError(getString(R.string.last_name_is_empty));
                    isValid = false;
                }else{
                    lastNameInputLayout.setError(null);
                }

                if (!email.contains("@") || !email.contains(".")) {
                    emailInputLayout.setError(getString(R.string.enter_valid_email));
                    isValid = false;
                }else{
                    emailInputLayout.setError(null);
                }

                if (idnp.length() != 13) {
                    idnpInputLayout.setError(getString(R.string.idnp_length_error));
                    isValid = false;
                }else {
                    idnpInputLayout.setError(null);
                }

                if (password.length() <= 6) {
                    passwordInputLayout.setError(getString(R.string.password_length_error));
                    isValid = false;
                } else if (!password.equals(confirmPassword)) {
                    passwordInputLayout.setError(getString(R.string.no_match_password_message));
                    confirmPasswordInputLayout.setError(getString(R.string.no_match_password_message));
                    isValid = false;
                }else {
                    passwordInputLayout.setError(null);
                    confirmPasswordInputLayout.setError(null);
                }

                if (isValid){
                    if (isAddDialog){
                        userViewModel.checkEmailExistence(email, new EmailExistCallback() {
                            @Override
                            public void onResult(boolean exists) {
                                if (!exists){
                                    userViewModel.checkPassportExistence(idnp, new PassportExistCallback() {
                                        @Override
                                        public void onResult(boolean exists) {
                                            if (!exists){
                                                User user = new User(null, firstName, lastName, email, idnp, role, password);
                                                userViewModel.addUser(view, alertDialog, user);
                                            }else {
                                                idnpInputLayout.setError(getText(R.string.passport_already_exists));
                                            }
                                        }
                                    }, idnpInputLayout, getActivity());

                                }else{
                                    emailInputLayout.setError(getText(R.string.email_already_registered));
                                }
                            }
                        });
                    }else{
                        User newUser = new User(userToUpdate.getId(), firstName, lastName,
                                userToUpdate.getEmail(), userToUpdate.getIdnp(), role, password);
                        userViewModel.editUser(view, newUser, userToUpdate, alertDialog);
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
    public void onStart() {
        super.onStart();
        userViewModel.getUsers();
    }

    @Override
    public void onButtonCancel() {
        bottomSheetFragment.dismiss();
    }

    @Override
    public void onButtonDelete(View view) {
        userViewModel.deleteUser(view.getContext(), userToDelete, getActivity(), bottomSheetFragment);
    }

    private void performSearch(String query) {
        // Dummy search operation
        searchResults.clear();
        for (User user : users) {
            if (user.getEmail().toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(user);
            }else if (user.getFirstName().toLowerCase().contains(query.toLowerCase())){
                searchResults.add(user);
            } else if (user.getLastName().toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(user);
            }
        }
            // Display search results
        if (!searchResults.isEmpty()){
            displayResults(searchResults);
        }else{
            Snackbar snackbar = Snackbar.make(getView(), "No results!", 2000);
            snackbar.show();
        }

    }

    private void updateSearchResults(String newText) {
        // logic to update search results dynamically
        // based on the user's input

        List<User> updatedResults = new ArrayList<>();
        for (User user : users) {
            if (user.getEmail().toLowerCase().contains(newText.toLowerCase())) {
                updatedResults.add(user);
            }else if (user.getFirstName().toLowerCase().contains(newText.toLowerCase())){
                updatedResults.add(user);
            } else if (user.getLastName().toLowerCase().contains(newText.toLowerCase())) {
                updatedResults.add(user);
            }
        }

        if (!updatedResults.isEmpty()){
            userAdapter.updateList(updatedResults);
        }

    }

    private void displayResults(List<User> results) {
        // logic to display the search results
        userAdapter.updateList(results);
    }
}