package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ulimorar.R;
import com.example.ulimorar.adapters.FacultyAdapter;
import com.example.ulimorar.adapters.UserAdapter;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.fragments.interfaces.BottomSheetListener;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.example.ulimorar.utils.controllers.SwipeController;
import com.example.ulimorar.utils.controllers.SwipeControllerActions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UsersActivity extends AppCompatActivity implements BottomSheetListener {

    private AlertDialog alertDialog;

    private DatabaseReference userDbReference;
    private FirebaseAuth auth;
    private String authenticatedUserEmail;

    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputLayout firstNameInputLayout;
    private TextInputLayout lastNameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout idnpInputLayout;
    private FloatingActionButton addUserButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Spinner roleSpinner;

    private RecyclerView userRecyclerView;
    private UserAdapter userAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<User> users;

    private SwipeController swipeController;

    private User currentUser;
    private User userToUpdate;
    private DeleteBottomSheetFragment bottomSheetFragment;
    private User userToDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.users_activity_title);
        setContentView(R.layout.activity_users);

        authenticatedUserEmail = getIntent().getStringExtra("currentUserEmail");
//      "users" is the name of the module to storage data
        userDbReference = FirebaseDatabase.getInstance().getReference("users");

        auth = FirebaseAuth.getInstance();

        addUserButton = findViewById(R.id.addUserFloatingButton);

        setupRecyclerView();

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(R.string.add_user_dialog_title, true, null);
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUsers();
            }
        });

    }

    private void setupRecyclerView() {
        userRecyclerView = findViewById(R.id.usersRecyclerView);
        users = new ArrayList<>();
        layoutManager = new LinearLayoutManager(this);
        userRecyclerView.setHasFixedSize(true);
        userRecyclerView.setLayoutManager(layoutManager);
        userAdapter = new UserAdapter(this, users);
        userRecyclerView.setAdapter(userAdapter);

        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked (int position){
                userToDelete = userAdapter.getUsers().get(position);
                bottomSheetFragment = new DeleteBottomSheetFragment();
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                bottomSheetFragment.setBottomSheetListener(UsersActivity.this);
            }

            @Override
            public void onLeftClicked(int position) {
                openDialog(R.string.edit_user_dialog_title, false, position);
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

    private void openDialog(int dialogTitle, boolean isAddDialog, Integer itemPosition) {

        View alertDialogCustomView = LayoutInflater.from(this).inflate(R.layout.add_edit_user_dialog, null);

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
                this,
                R.array.roleSpinnerArray,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        roleSpinner.setAdapter(adapter);

        // Personalized dialog interface
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

        // Show dialog
        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        if (!isAddDialog){
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
                    isValid = false; // Mark as invalid
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
                        addUser(firstName, lastName, email, idnp, role, password);
                    }else{
                        editUser(userToUpdate.getId(), firstName, lastName, email, idnp, role, password);
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

    private void getUserByEmail(String userEmail) {
        Query query = userDbReference.orderByChild("email").equalTo(userEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User found
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Access user data
                        User authenticatedUserFromDb = snapshot.getValue(User.class);
                        if (authenticatedUserFromDb != null){
                            currentUser = authenticatedUserFromDb;
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

    private void getUsers() {
        Query query = FirebaseDatabase.getInstance().getReference("users");
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                users.clear();  // because everytime when data updates in your firebase database it creates the list with updated items
                // so to avoid duplicate fields we clear the list everytime
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        assert user != null;
                        if (user.getEmail() != null){
                            if (!user.getEmail().equals(Objects.requireNonNull(auth.getCurrentUser()).getEmail())){
                                users.add(user);
                            }
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void addUser(String firstName, String lastName, String email, String idnp, String role, String password) {

        FirebaseUser firebaseUser = auth.getCurrentUser();
        getUserByEmail(firebaseUser.getEmail());

            String userId = userDbReference.push().getKey();

            User user = new User(userId, firstName, lastName, email, idnp, role, password);

            // Add the user to the database
            userDbReference.child(userId).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // If user added successfully, add the same user credentials to Firebase Authentication
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(authTask -> {
                                if (authTask.isSuccessful()) {
                                    Toast.makeText(UsersActivity.this, R.string.add_user_successful_message, Toast.LENGTH_SHORT).show();
                                    auth.signInWithEmailAndPassword(currentUser.getEmail(), currentUser.getPassword());
                                } else {
                                    Toast.makeText(UsersActivity.this, R.string.add_user_failure_message, Toast.LENGTH_SHORT).show();
                                    Log.d("FailureAddUser", authTask.getException().getMessage());
                                }
                            });

                    alertDialog.dismiss();
                } else {
                    Toast.makeText(UsersActivity.this, R.string.add_user_failure_message, Toast.LENGTH_SHORT).show();
                    Log.d("FailureAddUser", task.getException().getMessage());
                }
            });
    }

    public void deleteUserByEmail(Context context, User userToDelete) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        getUserByEmail(firebaseUser.getEmail());

        // Asigurați-vă că utilizatorul curent există și nu este utilizatorul pe care încercați să-l ștergeți
        if (!firebaseUser.getEmail().equals(userToDelete.getEmail())) {
            // Autentificați-vă cu adresa de e-mail a utilizatorului pe care doriți să-l ștergeți
            auth.signInWithEmailAndPassword(userToDelete.getEmail(), userToDelete.getPassword())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            user.delete();

                            // Căutați utilizatorul în baza de date după adresa de e-mail
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
                            Query query = databaseReference.orderByChild("email").equalTo(userToDelete.getEmail());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        // Obțineți cheia utilizatorului găsit și ștergeți-l din baza de date
                                        String userKey = snapshot.getKey();
                                        DatabaseReference userReference = databaseReference.child(userKey);
                                        userReference.removeValue()
                                                .addOnSuccessListener(aVoid -> {
                                                    // Utilizatorul a fost șters cu succes din baza de date
                                                    String message = getText(R.string.user) + " " + userToDelete.getFirstName() + " " +
                                                            userToDelete.getLastName() + " " + getText(R.string.delete_user_success);
                                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                                    auth.signInWithEmailAndPassword(currentUser.getEmail(), currentUser.getPassword());
                                                    bottomSheetFragment.dismiss();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Tratarea erorii la ștergerea din Realtime Database
                                                    Toast.makeText(context, R.string.delete_user_from_db_fail, Toast.LENGTH_LONG).show();
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Tratarea erorii la interogarea bazei de date
                                    Toast.makeText(context, R.string.database_query_error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Tratarea erorii la autentificarea în contul utilizatorului
                            Toast.makeText(context, R.string.authentication_error, Toast.LENGTH_SHORT).show();
                            auth.signInWithEmailAndPassword(currentUser.getEmail(), currentUser.getPassword());
                        }
                    });
        } else {
            // Tratarea cazului în care utilizatorul curent este null sau este utilizatorul curent
            Toast.makeText(context, R.string.delete_current_user_error, Toast.LENGTH_SHORT).show();
            auth.signInWithEmailAndPassword(currentUser.getEmail(), currentUser.getPassword());
        }
    }

    public void editUser(String userId, String firstName, String lastName, String email, String idnp, String role, String password){

        FirebaseUser firebaseUser = auth.getCurrentUser();
        getUserByEmail(firebaseUser.getEmail());

        User newUser = new User(userId, firstName, lastName, email, idnp, role, password);

            userDbReference.child(userId).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    auth.signInWithEmailAndPassword(userToUpdate.getEmail(), userToUpdate.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                FirebaseUser oldUser = auth.getCurrentUser();
                                oldUser.delete();
                                auth.createUserWithEmailAndPassword(newUser.getEmail(), newUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                        Toast.makeText(UsersActivity.this, R.string.update_user_success_message, Toast.LENGTH_SHORT).show();
                                        auth.signInWithEmailAndPassword(currentUser.getEmail(), currentUser.getPassword());
                                    }
                                });
                            }
                        }
                    });
                    alertDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(UsersActivity.this, R.string.update_user_failure, Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUsers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.users).setVisible(false);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection.
        switch (item.getItemId()) {
            case android.R.id.home: finish();
                return true;
            case R.id.faculty:
                startActivity(new Intent(UsersActivity.this, FacultyActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            case R.id.logout:
                Toast.makeText(this, R.string.logout_message, Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UsersActivity.this, LoginActivity.class));
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
        deleteUserByEmail(UsersActivity.this, userToDelete);
    }
}