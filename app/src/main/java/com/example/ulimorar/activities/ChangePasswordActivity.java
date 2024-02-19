package com.example.ulimorar.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.ulimorar.R;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.viewmodels.UserViewModel;
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

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    private Button cancelButton;
    private Button saveButton;

    private User currentUser;

//    private FirebaseAuth auth;
//    private DatabaseReference databaseReference;

    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getText(R.string.change_password));
        setContentView(R.layout.activity_change_password);

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

//        auth = FirebaseAuth.getInstance();
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        passwordInputLayout = findViewById(R.id.passwordTextField);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordTextField);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

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

    private void validateAndSaveData() {
        String password = passwordInputLayout.getEditText().getText().toString().trim();
        String confirmPassword = confirmPasswordInputLayout.getEditText().getText().toString().trim();

        boolean isValid = isValidEmail(password, confirmPassword);

        if (!confirmPassword.equals(currentUser.getPassword())){
            if (isValid) {
//                saveData(confirmPassword);
                User newUser = new User(currentUser.getId(), currentUser.getFirstName(),
                        currentUser.getLastName(), currentUser.getEmail(), currentUser.getIdnp(),
                        currentUser.getRole(), confirmPassword);

                userViewModel.deleteOldUserAndSignInWithNewPassword(this, currentUser, newUser);
            }
        }else{
            confirmPasswordInputLayout.setError(getString(R.string.this_is_your_old_password));
        }
    }

    private boolean isValidEmail(String password, String confirmPassword) {

        if (password.length() <= 6) {
            passwordInputLayout.setError(getString(R.string.password_length_error));
           return false;
        } else if (!password.equals(confirmPassword)) {
            passwordInputLayout.setError(getString(R.string.no_match_password_message));
            confirmPasswordInputLayout.setError(getString(R.string.no_match_password_message));
            return false;
        }else {
            passwordInputLayout.setError(null);
            confirmPasswordInputLayout.setError(null);
            return true;
        }
    }

//    private void saveData(String newPassword) {
//        User newUser = new User(currentUser.getId(), currentUser.getFirstName(),
//                currentUser.getLastName(), currentUser.getEmail(), currentUser.getIdnp(),
//                currentUser.getRole(), newPassword);
//
//        deleteOldUserAndSignInWithNew(currentUser, newUser);
//    }

//    private void deleteOldUserAndSignInWithNew(User oldUser, User newUser){
//        auth.signInWithEmailAndPassword(oldUser.getEmail(), oldUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
//                if (task.isSuccessful()) {
//                    FirebaseUser user = auth.getCurrentUser();
//                    user.delete();
//                    createNewUserInAuth(newUser, oldUser.getId());
//                }
//            }
//        });
//    }

//    private void signInWithNewDataAndReload(User newUser){
//        auth.signInWithEmailAndPassword(newUser.getEmail(), newUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
//                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), getText(R.string.user_data_changed), 3000);
//                snackbar.show();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(4000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        FirebaseAuth.getInstance().signOut();
//                        startActivity(new Intent(ChangePasswordActivity.this, LoginActivity.class));
//                        finish();
//                    }
//                }).start();
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull @NotNull Exception e) {
//                Toast.makeText(ChangePasswordActivity.this, R.string.error_signin_with_new_credentials, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void createNewUserInAuth(User newUser, String userId){
//        auth.createUserWithEmailAndPassword(newUser.getEmail(), newUser.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
//                databaseReference.child(userId).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull @NotNull Task<Void> task) {
//                        Toast.makeText(ChangePasswordActivity.this, R.string.update_user_success_message, Toast.LENGTH_SHORT).show();
//                        signInWithNewDataAndReload(newUser);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull @NotNull Exception e) {
//                        Toast.makeText(ChangePasswordActivity.this, R.string.update_user_failure, Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull @NotNull Exception e) {
//                Toast.makeText(ChangePasswordActivity.this, R.string.error_create_user_in_auth, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

}