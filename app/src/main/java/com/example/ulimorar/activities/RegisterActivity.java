package com.example.ulimorar.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.example.ulimorar.R;
import com.example.ulimorar.callbacks.EmailExistCallback;
import com.example.ulimorar.callbacks.PassportExistCallback;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.entities.enums.UserRole;
import com.example.ulimorar.utils.GetDialogsStandardButtons;
import com.example.ulimorar.viewmodels.PassportIdViewModel;
import com.example.ulimorar.viewmodels.UserViewModel;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout idnpTextInputLayout;
    private TextInputLayout firstNameInputLayout;
    private TextInputLayout lastNameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    private Button cancelButton;
    private Button nextButton;

    private UserViewModel userViewModel;
    private PassportIdViewModel passportIdViewModel;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getText(R.string.register));
        setContentView(R.layout.activity_register);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        passportIdViewModel = new ViewModelProvider(this).get(PassportIdViewModel.class);

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
                    passportIdViewModel.checkPassportToRegisterExistence(passportId, new PassportExistCallback() {
                        @Override
                        public void onResult(boolean exists) {
                            if (exists){
                                openRegisterForm(passportId);
                            }else {
                                idnpTextInputLayout.setError(getText(R.string.passport_id_is_not_supported));
                            }
                        }
                    });
                }
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

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

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
                    isValid = false;
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
                    userViewModel.checkEmailExistence(email, new EmailExistCallback() {
                        @Override
                        public void onResult(boolean exists) {
                            if (!exists){
                                User user = new User(null, firstName, lastName, email, passportId, UserRole.STUDENT.name(), password);
                                userViewModel.registerUser(RegisterActivity.this, view, alertDialog, user);
                            }else {
                                emailInputLayout.setError(getText(R.string.email_already_registered));
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
}