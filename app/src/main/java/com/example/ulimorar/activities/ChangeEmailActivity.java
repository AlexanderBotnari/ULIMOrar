package com.example.ulimorar.activities;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import com.example.ulimorar.R;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.viewmodels.UserViewModel;
import com.google.android.material.textfield.TextInputLayout;

public class ChangeEmailActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout;

    private Button cancelButton;
    private Button saveButton;

    private User currentUser;

    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getText(R.string.change_email));
        setContentView(R.layout.activity_change_email);

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

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
                User newUser = new User(currentUser.getId(), currentUser.getFirstName(),
                        currentUser.getLastName(), email, currentUser.getIdnp(),
                        currentUser.getRole(), currentUser.getPassword());

                userViewModel.updateEmail(this, newUser, newUser.getId());
                userViewModel.deleteOldUserAndSignInWithNewEmail(this, currentUser, newUser);
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

    @Override
    protected void onStart() {
        super.onStart();
        emailInputLayout.getEditText().setText(currentUser.getEmail());
    }

}