package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FacultyActivity extends AppCompatActivity {

    private FloatingActionButton addFacultyButton;
    private TextView dialogTitleTextView;
    private EditText facultyNameEditText;
    private EditText facultyDescriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.faculty_activity_title);
        setContentView(R.layout.activity_faculty);

        addFacultyButton = findViewById(R.id.addFacultyFloatingButton);
        dialogTitleTextView = findViewById(R.id.dialogTitleTextView);
        facultyNameEditText = findViewById(R.id.facultyNameEditText);
        facultyDescriptionEditText = findViewById(R.id.facultyDescriptionEditText);

        addFacultyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCustomDialog();
            }
        });
    }

    private void openCustomDialog() {
        // Obțineți referință la LayoutInflater
        LayoutInflater inflater = LayoutInflater.from(this);
        // Inflate layout-ul dialogului
        View dialogView = inflater.inflate(R.layout.add_edit_faculty_dialog, null);

        // Configurați TextView-ul pentru titlu
        TextView titleTextView = dialogView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText("Add Faculty");
        titleTextView.setTextSize(24);
        titleTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);

        // Configurați EditText-urile
        EditText facultyNameEditText = dialogView.findViewById(R.id.facultyNameEditText);
        EditText facultyDescriptionEditText = dialogView.findViewById(R.id.facultyDescriptionEditText);

        // Construiți interfața de dialog personalizată
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Adăugați butoanele "Cancel" și "Save"
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Acțiunea pentru butonul "Cancel"
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Acțiunea pentru butonul "Save"
                String facultyName = facultyNameEditText.getText().toString();
                String facultyDescription = facultyDescriptionEditText.getText().toString();

                // Implementați logica pentru salvare sau afișare a valorilor
                // ...
                Toast.makeText(FacultyActivity.this, facultyName + " " + facultyDescription, Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        // Afișați dialogul
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Setați culoarea butoanelor în mod programatic
        setButtonColors(alertDialog);
    }

    private void setButtonColors(AlertDialog alertDialog) {
        // Obțineți butoanele dialogului
        Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);

        // Setați culoarea textului pentru butonul "Cancel"
        negativeButton.setTextColor(Color.BLUE);

        // Setați culoarea textului pentru butonul "Save"
        positiveButton.setTextColor(Color.BLUE);
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
                Toast.makeText(this, "Faculty item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(FacultyActivity.this, FacultyActivity.class);
                startActivity(intent);
                return true;
            case R.id.chair:
                Toast.makeText(this, "Chair item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(FacultyActivity.this, ChairActivity.class);
                startActivity(intent);
                return true;
            case R.id.group:
                Toast.makeText(this, "Group item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(FacultyActivity.this, GroupActivity.class);
                startActivity(intent);
                return true;
            case R.id.timetable:
                Toast.makeText(this, "Timetable item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(FacultyActivity.this, TimetableActivity.class);
                startActivity(intent);
                return true;
            case R.id.users:
                Toast.makeText(this, "Users item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(FacultyActivity.this, UsersActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout:
                Toast.makeText(this, "Logout item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(FacultyActivity.this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}