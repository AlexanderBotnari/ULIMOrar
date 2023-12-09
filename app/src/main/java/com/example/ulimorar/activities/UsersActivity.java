package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;
import com.example.ulimorar.utils.GetDialogsStandartButtons;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class UsersActivity extends AppCompatActivity {

    private FloatingActionButton addUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.users_activity_title);
        setContentView(R.layout.activity_users);

        addUserButton = findViewById(R.id.addUserFloatingButton);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(R.string.add_user_dialog_title);
            }
        });
    }

    private void openDialog(int dialogTitle) {
        View alertDialogCustomView = LayoutInflater.from(this).inflate(R.layout.add_edit_user_dialog, null);

        TextView titleTextView = alertDialogCustomView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(dialogTitle);
        EditText firstNameEditText = alertDialogCustomView.findViewById(R.id.firstNameEditText);
        EditText lastNameEditText = alertDialogCustomView.findViewById(R.id.lastNameEditText);
        EditText idnpEditText = alertDialogCustomView.findViewById(R.id.idnpEditText);
        EditText passwordEditText = alertDialogCustomView.findViewById(R.id.passwordEditText);
        EditText confirmPasswordEditText = alertDialogCustomView.findViewById(R.id.confirmPasswordEditText);


        // Construiți interfața de dialog personalizată
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

        // Afișați dialogul
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        GetDialogsStandartButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(UsersActivity.this, firstNameEditText.getText().toString()+"\n"+
                        lastNameEditText.getText().toString()+"\n"+
                        idnpEditText.getText().toString()+"\n"+
                        passwordEditText.getText().toString()+"\n"+
                        confirmPasswordEditText.getText().toString()+"\n", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });

        GetDialogsStandartButtons.getCancelButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.faculty:
                startActivity(new Intent(UsersActivity.this, FacultyActivity.class));
                return true;
            case R.id.chair:
                startActivity(new Intent(UsersActivity.this, ChairActivity.class));
                return true;
            case R.id.group:
                startActivity(new Intent(UsersActivity.this, GroupActivity.class));
                return true;
            case R.id.timetable:
                startActivity(new Intent(UsersActivity.this, TimetableActivity.class));
                return true;
            case R.id.users:
                startActivity(new Intent(UsersActivity.this, UsersActivity.class));
                return true;
            case R.id.logout:
                Toast.makeText(this, R.string.logout_message, Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UsersActivity.this, LoginActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}