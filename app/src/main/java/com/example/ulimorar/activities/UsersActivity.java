package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;
import com.example.ulimorar.entities.User;
import com.example.ulimorar.utils.GetDialogsStandartButtons;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

public class UsersActivity extends AppCompatActivity {

    private FloatingActionButton addUserButton;

    private DatabaseReference userDbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.users_activity_title);
        setContentView(R.layout.activity_users);

//      "users" is the name of the module to storage data
        userDbReference = FirebaseDatabase.getInstance().getReference("users");

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
        EditText emailEditText = alertDialogCustomView.findViewById(R.id.emailEditText);
        EditText idnpEditText = alertDialogCustomView.findViewById(R.id.idnpEditText);
        EditText passwordEditText = alertDialogCustomView.findViewById(R.id.passwordEditText);
        EditText confirmPasswordEditText = alertDialogCustomView.findViewById(R.id.confirmPasswordEditText);

        // Personalized dialog interface
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertDialogCustomView);

        // Show dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        GetDialogsStandartButtons.getSaveButton(alertDialogCustomView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeNewUser(firstNameEditText.getText().toString(),
                            lastNameEditText.getText().toString(),
                            emailEditText.getText().toString(),
                            idnpEditText.getText().toString());
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

    public void writeNewUser(String firstName, String lastName, String email, String idnp) {
        // Generate unique ID for user
        String userId = userDbReference.push().getKey();
        User user = new User(userId, firstName, lastName, email, idnp);
        userDbReference.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(UsersActivity.this, "Successful added user", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(UsersActivity.this, "Failure to add user", Toast.LENGTH_SHORT).show();
                    Log.d("FailureAddUser", task.getException().getMessage());
                }
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