package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;
import com.example.ulimorar.activities.fragments.PassportIdsForUserRegistrationFragment;
import com.example.ulimorar.activities.fragments.RegisteredUsersFragment;

public class UsersActivity extends AppCompatActivity {
    private String authenticatedUserEmail;
    private Button registeredButton;
    private Button toRegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.users_activity_title);
        setContentView(R.layout.activity_users);

        authenticatedUserEmail = getIntent().getStringExtra("currentUserEmail");

        // Verify if fragment already added
        if (savedInstanceState == null) {
            // add first fragment to the activity
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegisteredUsersFragment())
                    .commit();
        }

        registeredButton = findViewById(R.id.registeredButton);
        toRegisterButton = findViewById(R.id.toRegisterButton);

        registeredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegisteredUsersFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        toRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PassportIdsForUserRegistrationFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.users).setVisible(false);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection.
        switch (item.getItemId()) {
            case android.R.id.home: finish();
                return true;
            case R.id.faculty:
                startActivity(new Intent(UsersActivity.this, FacultyActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            case R.id.account:
                startActivity(new Intent(UsersActivity.this, AccountActivity.class).putExtra("currentUserEmail", authenticatedUserEmail));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}