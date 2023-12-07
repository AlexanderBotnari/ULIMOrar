package com.example.ulimorar.activities;

import android.content.Intent;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_ULIMNoActionBar);
        setContentView(R.layout.activity_login);

        TextInputLayout loginInput = findViewById(R.id.idnpTextField);
        TextInputLayout passwordInput = findViewById(R.id.passwordTextField);
        Button loginButton = findViewById(R.id.loginButton);
        TextView forgotTextView = findViewById(R.id.forgotPasswordButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the text from the EditText
                String login = loginInput.getEditText().getText().toString().trim();
                String password = passwordInput.getEditText().getText().toString().trim();
                String message = "Login: " + login + " , Password: " + password;
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, FacultyActivity.class);
                startActivity(intent);
            }
        });

        forgotTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "Please contact your sys admin!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}