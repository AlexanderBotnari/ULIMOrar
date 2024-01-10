package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ulimorar.R;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.entities.enums.UserRole;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

public class AccountActivity extends AppCompatActivity {

    private String authenticatedUserEmail;
    private DatabaseReference userDbReference;
    private FirebaseAuth auth;

    private User authenticatedUser;

    private TextInputLayout firstNameTextField;
    private TextInputLayout lastNameTextField;
    private TextInputLayout emailTextField;
    private TextView idnpTextView;
    private TextInputLayout passwordTextField;
    private TextInputLayout confirmPasswordTextField;
    private Button editButton;
    private Button saveButton;
    private Button logoutButton;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.account_activity_title);
        setContentView(R.layout.activity_account);

        Intent intent = getIntent();
        authenticatedUserEmail = intent.getStringExtra("currentUserEmail");

        auth = FirebaseAuth.getInstance();
        userDbReference = FirebaseDatabase.getInstance().getReference("users");

        firstNameTextField = findViewById(R.id.firstNameTextField);
        lastNameTextField = findViewById(R.id.lastNameTextField);
        emailTextField = findViewById(R.id.emailTextField);
        passwordTextField = findViewById(R.id.passwordTextField);
        confirmPasswordTextField = findViewById(R.id.confirmPasswordTextField);
        idnpTextView = findViewById(R.id.idnpTextView);

        logoutButton = findViewById(R.id.logoutButton);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserByEmail(authenticatedUserEmail);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AccountActivity.this, R.string.logout_message, Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AccountActivity.this, LoginActivity.class));
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editUser();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUser();
            }
        });

    }

    private void makeFieldsEditable(boolean isEditable){
        firstNameTextField.setFocusable(isEditable);
        firstNameTextField.setEnabled(isEditable);
        firstNameTextField.getEditText().setCursorVisible(isEditable);

        lastNameTextField.setFocusable(isEditable);
        lastNameTextField.setEnabled(isEditable);
        lastNameTextField.getEditText().setCursorVisible(isEditable);

        emailTextField.setFocusable(isEditable);
        emailTextField.setEnabled(isEditable);
        emailTextField.getEditText().setCursorVisible(isEditable);

        passwordTextField.setFocusable(isEditable);
        passwordTextField.setEnabled(isEditable);
        passwordTextField.getEditText().setCursorVisible(isEditable);

        confirmPasswordTextField.setFocusable(isEditable);
        confirmPasswordTextField.setEnabled(isEditable);
        confirmPasswordTextField.getEditText().setCursorVisible(isEditable);

        saveButton.setFocusable(isEditable);
        saveButton.setEnabled(isEditable);

    }

    private void saveUser() {
        String firstName = firstNameTextField.getEditText().getText().toString();
        String lastName = lastNameTextField.getEditText().getText().toString();
        String email = emailTextField.getEditText().getText().toString();
        String password = passwordTextField.getEditText().getText().toString();
        String confirmPassword = confirmPasswordTextField.getEditText().getText().toString();

        boolean isValid = true;

        if (firstName.equals("")) {
            firstNameTextField.setError(getString(R.string.first_name_is_empty));
            isValid = false; // Mark as invalid
        }else{
            firstNameTextField.setError(null);
        }

        if (lastName.equals("")) {
            lastNameTextField.setError(getString(R.string.last_name_is_empty));
            isValid = false;
        }else{
            lastNameTextField.setError(null);
        }

        if (!email.contains("@") || !email.contains(".")) {
            emailTextField.setError(getString(R.string.enter_valid_email));
            isValid = false;
        }else{
            emailTextField.setError(null);
        }

        if (password.length() <= 6) {
            passwordTextField.setError(getString(R.string.password_length_error));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            passwordTextField.setError(getString(R.string.no_match_password_message));
            confirmPasswordTextField.setError(getString(R.string.no_match_password_message));
            isValid = false;
        }else {
            passwordTextField.setError(null);
            confirmPasswordTextField.setError(null);
        }

        if (isValid) {
            getUserByEmail(authenticatedUserEmail);
            String oldPassword = authenticatedUser.getPassword();
            String newPassword = email;

            User newUser = new User(authenticatedUser.getId(), firstName, lastName, email,
                    authenticatedUser.getIdnp(), authenticatedUser.getRole(), password);

            // if update only email or email and password
            if (!authenticatedUser.getEmail().equals(newUser.getEmail()) |
                    (!authenticatedUser.getEmail().equals(newUser.getEmail()) && !authenticatedUser.getPassword().equals(newUser.getPassword()))) {

                userDbReference.child(authenticatedUser.getId()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {

                        createNewUserInAuth(newUser);

                        deleteOldUserAndSignInWithNew(authenticatedUser, newUser);

                        makeFieldsEditable(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(AccountActivity.this, R.string.update_user_failure, Toast.LENGTH_SHORT).show();
                    }
                });

                // if update only password
            }

            if (!authenticatedUser.getPassword().equals(newUser.getPassword())){
                userDbReference.child(authenticatedUser.getId()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        auth.signInWithEmailAndPassword(authenticatedUser.getEmail(), oldPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser oldUser = auth.getCurrentUser();
                                    assert oldUser != null;
                                    oldUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {

                                            createNewUserInAuth(newUser);

                                            signInWithNewDataAndReload(newUser);
                                        }
                                    });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(AccountActivity.this, "Authentication data error", Toast.LENGTH_SHORT).show();
                            }
                        });

                        makeFieldsEditable(false);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(AccountActivity.this, R.string.update_user_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }

                // update user data without newEmail and newPassword
                userDbReference.child(authenticatedUser.getId()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(AccountActivity.this, R.string.update_user_success_message, Toast.LENGTH_SHORT).show();
                        makeFieldsEditable(false);
                    }
                });

        }
    }

    private void editUser() {
        makeFieldsEditable(true);
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
                        authenticatedUser = snapshot.getValue(User.class);

                        firstNameTextField.getEditText().setText(authenticatedUser.getFirstName());
                        lastNameTextField.getEditText().setText(authenticatedUser.getLastName());
                        emailTextField.getEditText().setText(authenticatedUser.getEmail());
                        idnpTextView.setText(authenticatedUser.getIdnp());
                        passwordTextField.getEditText().setText(authenticatedUser.getPassword());
                        confirmPasswordTextField.getEditText().setText(authenticatedUser.getPassword());

                    }

                    swipeRefreshLayout.setRefreshing(false);
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

    private void createNewUserInAuth(User newUser){
        auth.createUserWithEmailAndPassword(newUser.getEmail(), newUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(AccountActivity.this, R.string.error_create_user_in_auth, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithNewDataAndReload(User newUser){
        auth.signInWithEmailAndPassword(newUser.getEmail(), newUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getText(R.string.user_data_changed), 3000);
                snackbar.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        auth.signOut();
                        startActivity(new Intent(AccountActivity.this, LoginActivity.class));
                        finish();
                    }
                }).start();
            }
        });
//                .addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull @NotNull Exception e) {
//                Toast.makeText(AccountActivity.this, R.string.error_signin_with_new_credentials, Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void deleteOldUserAndSignInWithNew(User oldUser, User newUser){
        auth.signInWithEmailAndPassword(oldUser.getEmail(), oldUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser oldUser = auth.getCurrentUser();
                    oldUser.delete();
                    signInWithNewDataAndReload(newUser);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserByEmail(authenticatedUserEmail);
        makeFieldsEditable(false);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}