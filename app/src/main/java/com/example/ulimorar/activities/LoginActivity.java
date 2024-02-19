package com.example.ulimorar.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import com.example.ulimorar.R;
import com.example.ulimorar.activities.fragments.RegisteredUsersFragment;
import com.example.ulimorar.callbacks.EmailExistCallback;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.utils.EmailSender;
import com.example.ulimorar.viewmodels.UserViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity {

//    private static final String FAILURE_TAG = "FAILURE_TAG";

    private TextInputLayout loginInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText loginEditText;
    private TextInputEditText passwordEditText;

//    private FirebaseAuth auth;

//    private DatabaseReference usersDbReference;

    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_ULIMNoActionBar);
        try {
            setContentView(R.layout.activity_login);
        }catch (Exception ignore){

        }

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

//        auth = FirebaseAuth.getInstance();
//        usersDbReference = FirebaseDatabase.getInstance().getReference("users");

        loginInputLayout = findViewById(R.id.emailTextField);
        passwordInputLayout = findViewById(R.id.passwordTextField);
        loginEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        TextView forgotTextView = findViewById(R.id.forgotPasswordButton);
        TextView registerTextView = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(loginEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });

        forgotTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isValid = true;
                String login = loginEditText.getText().toString();
                if (!login.contains("@") || !login.contains(".")) {
                    loginInputLayout.setError(getString(R.string.enter_valid_email));
                    isValid = false;
                }else{
                    loginInputLayout.setError(null);
                }

                if (isValid){
                    checkEmailExistenceAndSendEmail(login, new EmailExistCallback() {
                        @Override
                        public void onResult(boolean exists) {
                            if (!exists){
                                loginInputLayout.setError(getString(R.string.email_not_registered));
                            }
                        }
                    });
                }
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

//    private interface EmailExistCallback{
//        void onResult(boolean exists);
//    }

    private void checkEmailExistenceAndSendEmail(String email, final EmailExistCallback callback) {

        userViewModel.checkEmailExistence(email, new EmailExistCallback() {
            @Override
            public void onResult(boolean exists) {
                if (exists){
                    User user = userViewModel.getUserByEmail(email);

                    if (user != null){
                        EmailSender emailSender = new EmailSender(LoginActivity.this);
                        emailSender.execute(user.getEmail(), user.getPassword());
                        callback.onResult(true);
                    }

                }
            }
        });

//        usersDbReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
//                        User user = childSnapshot.getValue(User.class);
//                        if (user != null) {
//                            EmailSender emailSender = new EmailSender(LoginActivity.this);
//                            emailSender.execute(user.getEmail(), user.getPassword());
//                            callback.onResult(true);
//                            return;
//                        }
//                    }
//                } else {
//                    callback.onResult(false);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                callback.onResult(false);
//            }
//        });
    }

    private void loginUser(String email, String password){
        if(loginEditText.getText().toString().trim().equals("")){
            loginInputLayout.setError(getText(R.string.empty_email_error));
        }else if(passwordEditText.getText().toString().trim().length() < 7){
            passwordInputLayout.setError(getText(R.string.password_length_error));
        } else {
            userViewModel.loginUser(this, email, password);
//            auth.signInWithEmailAndPassword(email, password)
//                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (task.isSuccessful()) {
//                                // Sign in success, update UI with the signed-in user's information
//                                FirebaseUser user = auth.getCurrentUser();
//                                startActivity(new Intent(LoginActivity.this, FacultyActivity.class).putExtra("currentUserEmail", user.getEmail()));
//                            } else {
//                                // If sign in fails, display a message to the user.
//                                Log.w(FAILURE_TAG, "signInWithEmail:failure", task.getException());
//                                Toast.makeText(LoginActivity.this, R.string.authentication_error,
//                                        Toast.LENGTH_LONG).show();
//                            }
//                        }
//                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = userViewModel.getLoggedInUser();
        if(currentUser != null){
            Intent intent = new Intent(LoginActivity.this, FacultyActivity.class);
            intent.putExtra("currentUserEmail", currentUser.getEmail());
            startActivity(intent);
        }
    }

}