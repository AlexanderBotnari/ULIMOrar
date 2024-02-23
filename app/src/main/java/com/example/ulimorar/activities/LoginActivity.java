package com.example.ulimorar.activities;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import com.example.ulimorar.R;
import com.example.ulimorar.callbacks.EmailExistCallback;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.utils.EmailSender;
import com.example.ulimorar.viewmodels.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout loginInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText loginEditText;
    private TextInputEditText passwordEditText;

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

    }

    private void loginUser(String email, String password){
        if(loginEditText.getText().toString().trim().equals("")){
            loginInputLayout.setError(getText(R.string.empty_email_error));
        }else if(passwordEditText.getText().toString().trim().length() < 7){
            passwordInputLayout.setError(getText(R.string.password_length_error));
        } else {
            userViewModel.loginUser(this, email, password);
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