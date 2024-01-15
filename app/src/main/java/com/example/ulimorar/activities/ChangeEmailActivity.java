package com.example.ulimorar.activities;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;
import com.example.ulimorar.entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.jetbrains.annotations.NotNull;

public class ChangeEmailActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout;

    private Button cancelButton;
    private Button saveButton;

    private User currentUser;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getText(R.string.change_email));
        setContentView(R.layout.activity_change_email);

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        emailInputLayout = findViewById(R.id.emailTextField);

        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndSaveData();
            }
        });

    }

    // Function to validate email and save data
    private void validateAndSaveData() {
        String email = emailInputLayout.getEditText().getText().toString().trim();

        boolean isValid = isValidEmail(email);

        if (!email.equals(currentUser.getEmail())){
            if (isValid) {
                saveData(email);
            }
        }else{
            emailInputLayout.setError(getString(R.string.this_is_your_current_email));
        }
    }

    // Function to check if the email is valid
    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError(getString(R.string.enter_valid_email));
            return false;
        } else {
            emailInputLayout.setError(null);
            return true;
        }
    }


    private void saveData(String newEmail) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        emailInputLayout.getEditText().setText(currentUser.getEmail());
    }

    private void createNewUserInAuth(User newUser, String userId){
        auth.createUserWithEmailAndPassword(newUser.getEmail(), newUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                databaseReference.child(userId).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(ChangeEmailActivity.this, R.string.update_user_success_message, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(ChangeEmailActivity.this, R.string.update_user_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(ChangeEmailActivity.this, R.string.error_create_user_in_auth, Toast.LENGTH_SHORT).show();
            }
        });
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
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(ChangeEmailActivity.this, LoginActivity.class));
                        finish();
                    }
                }).start();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(ChangeEmailActivity.this, R.string.error_signin_with_new_credentials, Toast.LENGTH_SHORT).show();
            }
        });
    }
}