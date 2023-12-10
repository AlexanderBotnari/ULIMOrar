package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.utils.GetDialogsStandartButtons;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

public class UsersActivity extends AppCompatActivity {

    private FloatingActionButton addUserButton;

    private AlertDialog alertDialog;

    private DatabaseReference userDbReference;
    private FirebaseAuth auth;

    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputLayout firstNameInputLayout;
    private TextInputLayout lastNameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout idnpInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.users_activity_title);
        setContentView(R.layout.activity_users);

//      "users" is the name of the module to storage data
        userDbReference = FirebaseDatabase.getInstance().getReference("users");

        auth = FirebaseAuth.getInstance();

        addUserButton = findViewById(R.id.addUserFloatingButton);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(R.string.add_user_dialog_title);
            }
        });
    }

    private void openDialog(int dialogTitle) {
        View alertDialogCustomView = LayoutInflater.from(this).inflate(R.layout.add_edit_user_dialog, null);

        TextView titleTextView = alertDialogCustomView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(dialogTitle);
        firstNameInputLayout = alertDialogCustomView.findViewById(R.id.firstNameTextField);
        lastNameInputLayout = alertDialogCustomView.findViewById(R.id.lastNameTextField);
        emailInputLayout = alertDialogCustomView.findViewById(R.id.emailTextField);
        idnpInputLayout = alertDialogCustomView.findViewById(R.id.idnpTextField);
        passwordInputLayout = alertDialogCustomView.findViewById(R.id.passwordTextField);
        confirmPasswordInputLayout = alertDialogCustomView.findViewById(R.id.confirmPasswordTextField);

        // Personalized dialog interface
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

        // Show dialog
        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        GetDialogsStandartButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = firstNameInputLayout.getEditText().getText().toString();
                String lastName = lastNameInputLayout.getEditText().getText().toString();
                String email = emailInputLayout.getEditText().getText().toString();
                String idnp = idnpInputLayout.getEditText().getText().toString();
                String password = passwordInputLayout.getEditText().getText().toString();
                String confirmPassword = confirmPasswordInputLayout.getEditText().getText().toString();
                addUser(firstName, lastName, email, idnp, password, confirmPassword);
            }
        });

        GetDialogsStandartButtons.getCancelButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    public void addUser(String firstName, String lastName, String email, String idnp, String password, String confirmPassword) {
        // Validate input data
        boolean isValid = true; // Add a flag to track overall validity

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

        if (isValid) { // Proceed only if all input is valid
            // Generate unique ID for user
            String userId = userDbReference.push().getKey();

            User user = new User(userId, firstName, lastName, email, idnp, password);

            // Add the user to the database
            userDbReference.child(userId).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // If user added successfully, add the same user credentials to Firebase Authentication
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(authTask -> {
                                if (authTask.isSuccessful()) {
                                    Toast.makeText(UsersActivity.this, R.string.add_user_successful_message, Toast.LENGTH_SHORT).show();
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
                startActivity(new Intent(UsersActivity.this, FacultyActivity.class));
                return true;
            case R.id.chair:
                startActivity(new Intent(UsersActivity.this, ChairActivity.class));
                return true;
            case R.id.group:
                startActivity(new Intent(UsersActivity.this, GroupActivity.class));
                return true;
            case R.id.timetable:
                startActivity(new Intent(UsersActivity.this, TimetableActivity.class));
                return true;
            case R.id.users:
                startActivity(new Intent(UsersActivity.this, UsersActivity.class));
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
}