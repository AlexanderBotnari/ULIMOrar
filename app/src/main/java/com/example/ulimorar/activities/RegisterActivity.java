package com.example.ulimorar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ulimorar.R;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.entities.enums.UserRole;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout idnpTextInputLayout;
    private TextInputLayout firstNameInputLayout;
    private TextInputLayout lastNameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    private Button cancelButton;
    private Button nextButton;

    private DatabaseReference idnpsDatabaseReference;
    private DatabaseReference usersDatabaseReference;

    private FirebaseAuth auth;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getText(R.string.register));
        setContentView(R.layout.activity_register);

        idnpsDatabaseReference = FirebaseDatabase.getInstance().getReference("passportIds");
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();

        idnpTextInputLayout = findViewById(R.id.idnpTextField);
        cancelButton = findViewById(R.id.cancelButton);
        nextButton = findViewById(R.id.nextButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String passportId = idnpTextInputLayout.getEditText().getText().toString().trim();
                if (passportId.length() != 13) {
                    idnpTextInputLayout.setError(getString(R.string.idnp_length_error));
                }else {
                    idnpTextInputLayout.setError(null);
                    checkPassportId(passportId);
                }
            }
        });
    }

    private void checkPassportId(String passportId){
        // Check if the passport ID already exists in the database to register
        idnpsDatabaseReference.child(passportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Passport ID already exists
                    openRegisterForm(passportId);
                } else {
                    // Passport ID does not exist
                    idnpTextInputLayout.setError(getText(R.string.passport_id_is_not_supported));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if necessary
                Log.e("DatabaseError", "Error checking passport ID existence: " + databaseError.getMessage());
            }
        });
    }

    private interface EmailExistCallback{
        void onResult(boolean exists);
    }

    private void checkEmailExistence(String email, EmailExistCallback callback){
        usersDatabaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    emailInputLayout.setError(getText(R.string.email_already_registered));
                }
                callback.onResult(snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(false);
            }
        });
    }

    private void openRegisterForm(String passportId) {

        View alertDialogCustomView = LayoutInflater.from(this).inflate(R.layout.register_form_layout, null);

        firstNameInputLayout = alertDialogCustomView.findViewById(R.id.firstNameTextField);
        lastNameInputLayout = alertDialogCustomView.findViewById(R.id.lastNameTextField);
        emailInputLayout = alertDialogCustomView.findViewById(R.id.emailTextField);
        passwordInputLayout = alertDialogCustomView.findViewById(R.id.passwordTextField);
        confirmPasswordInputLayout = alertDialogCustomView.findViewById(R.id.confirmPasswordTextField);

        // Personalized dialog interface
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

        // Show dialog
        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        GetDialogsStandardButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = firstNameInputLayout.getEditText().getText().toString();
                String lastName = lastNameInputLayout.getEditText().getText().toString();
                String email = emailInputLayout.getEditText().getText().toString();
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
                    checkEmailExistence(email, new EmailExistCallback() {
                        @Override
                        public void onResult(boolean exists) {
                            if (!exists){
                                addUser(firstName, lastName, email, passportId, password);
                            }
                        }
                    });
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

    private void addUser(String firstName, String lastName, String email, String passportId, String password) {

        String userId = usersDatabaseReference.push().getKey();

        User user = new User(userId, firstName, lastName, email, passportId, UserRole.STUDENT.name(), password);

        // Add the user to the database
        usersDatabaseReference.child(userId).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // If user added successfully, add the same user credentials to Firebase Authentication
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, R.string.successful_registration, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                                deletePassportIdFromDb(passportId);
                                auth.signOut();
                            } else {
                                Toast.makeText(RegisterActivity.this, R.string.registration_failure, Toast.LENGTH_SHORT).show();
                                Log.d("FailureAddUser", authTask.getException().getMessage());
                            }
                        });

                alertDialog.dismiss();
            } else {
                Toast.makeText(RegisterActivity.this, R.string.registration_failure, Toast.LENGTH_SHORT).show();
                Log.d("FailureAddUser", task.getException().getMessage());
            }
        });
    }

    public void deletePassportIdFromDb(String passportId){
        idnpsDatabaseReference.child(passportId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    idnpsDatabaseReference.child(passportId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                        }
                    });

                } else {
                    Toast.makeText(RegisterActivity.this, R.string.passport_not_exists, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error if necessary
                Log.e("DatabaseError", "Error checking passport ID existence: " + databaseError.getMessage());
            }
        });
    }

}