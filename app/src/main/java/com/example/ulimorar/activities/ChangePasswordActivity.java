package com.example.ulimorar.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.ulimorar.R;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.viewmodels.UserViewModel;
import com.google.android.material.textfield.TextInputLayout;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    private Button cancelButton;
    private Button saveButton;

    private User currentUser;

    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getText(R.string.change_password));
        setContentView(R.layout.activity_change_password);

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

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

}