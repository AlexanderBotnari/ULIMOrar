package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.ulimorar.R;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.viewmodels.UserViewModel;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class AccountActivity extends AppCompatActivity {

    private String authenticatedUserEmail;

    private UserViewModel userViewModel;

    private User authenticatedUser;

    private TextInputLayout firstNameTextField;
    private TextInputLayout lastNameTextField;
    private TextView idnpTextView;
    private TextView passwordTextView;
    private TextView emailTextView;
    private Button editButton;
    private Button saveButton;
    private Button logoutButton;
    private ImageButton editEmailImageButton;
    private ImageButton editPasswordImageButton;
    private ImageButton seePasswordImageButton;
    private ImageButton hidePasswordImageButton;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.account_activity_title);
        setContentView(R.layout.activity_account);

        Intent intent = getIntent();
        authenticatedUserEmail = intent.getStringExtra("currentUserEmail");

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        firstNameTextField = findViewById(R.id.firstNameTextField);
        lastNameTextField = findViewById(R.id.lastNameTextField);
        idnpTextView = findViewById(R.id.idnpTextView);
        passwordTextView = findViewById(R.id.passwordTextView);
        emailTextView = findViewById(R.id.emailTextView);

        logoutButton = findViewById(R.id.logoutButton);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);

        editEmailImageButton = findViewById(R.id.editEmailImageButton);
        editPasswordImageButton = findViewById(R.id.editPasswordImageButton);
        seePasswordImageButton = findViewById(R.id.seePasswordImageButton);
        hidePasswordImageButton = findViewById(R.id.hidePasswordImageButton);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getUserByEmailAndSetDataInFields(authenticatedUserEmail);
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

        editEmailImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AccountActivity.this, ChangeEmailActivity.class).putExtra("currentUser", authenticatedUser));
            }
        });

        editPasswordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AccountActivity.this, ChangePasswordActivity.class).putExtra("currentUser", authenticatedUser));
            }
        });

        seePasswordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordTextView.setTransformationMethod(null);
                hidePasswordImageButton.setVisibility(View.VISIBLE);
                seePasswordImageButton.setVisibility(View.GONE);
            }
        });

        hidePasswordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordTextView.setTransformationMethod(PasswordTransformationMethod.getInstance());
                seePasswordImageButton.setVisibility(View.VISIBLE);
                hidePasswordImageButton.setVisibility(View.GONE);
            }
        });

    }

    public void makeFieldsEditable(boolean isEditable){
        firstNameTextField.setFocusable(isEditable);
        firstNameTextField.setEnabled(isEditable);
        firstNameTextField.getEditText().setCursorVisible(isEditable);

        lastNameTextField.setFocusable(isEditable);
        lastNameTextField.setEnabled(isEditable);
        lastNameTextField.getEditText().setCursorVisible(isEditable);

        saveButton.setFocusable(isEditable);
        saveButton.setEnabled(isEditable);

    }

    private void saveUser() {
        String firstName = firstNameTextField.getEditText().getText().toString();
        String lastName = lastNameTextField.getEditText().getText().toString();

        boolean isValid = true;

        if (firstName.equals("")) {
            firstNameTextField.setError(getString(R.string.first_name_is_empty));
            isValid = false;
        }else{
            firstNameTextField.setError(null);
        }

        if (lastName.equals("")) {
            lastNameTextField.setError(getString(R.string.last_name_is_empty));
            isValid = false;
        }else{
            lastNameTextField.setError(null);
        }

        if (isValid) {
            getUserByEmailAndSetDataInFields(authenticatedUserEmail);

            User newUser = new User(authenticatedUser.getId(), firstName, lastName, authenticatedUser.getEmail(),
                    authenticatedUser.getIdnp(), authenticatedUser.getRole(), authenticatedUser.getPassword());

            userViewModel.updateFirstAndLastName(AccountActivity.this, authenticatedUser.getId(), newUser);
        }
    }

    private void editUser() {
        makeFieldsEditable(true);
    }

    private void getUserByEmailAndSetDataInFields(String userEmail) {

                        userViewModel.getUserByEmailLiveData(userEmail).observe(this, new Observer<User>() {
                            @Override
                            public void onChanged(User user) {
                                if (user != null){
                                    authenticatedUser = user;

                                    firstNameTextField.getEditText().setText(user.getFirstName());
                                    lastNameTextField.getEditText().setText(user.getLastName());
                                    idnpTextView.setText(user.getIdnp());
                                    emailTextView.setText(user.getEmail());
                                    passwordTextView.setText(user.getPassword());

                                    swipeRefreshLayout.setRefreshing(false);
                                    makeFieldsEditable(false);
                                }
                            }
                        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserByEmailAndSetDataInFields(authenticatedUserEmail);
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