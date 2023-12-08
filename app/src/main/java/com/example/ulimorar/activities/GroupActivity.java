package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;
import com.example.ulimorar.utils.GetDialogsStandartButtons;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GroupActivity extends AppCompatActivity {

    private FloatingActionButton addGroupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.groups_activity_title);
        setContentView(R.layout.activity_group);

        addGroupButton = findViewById(R.id.addGroupFloatingButton);
        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog(R.string.add_group_dialog_title);
            }
        });
    }

    private void openDialog(int dialogTitle) {
        View alertDialogCustomView = LayoutInflater.from(this).inflate(R.layout.add_edit_group_dialog, null);

        TextView titleTextView = alertDialogCustomView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(dialogTitle);
        EditText groupNameEditText = alertDialogCustomView.findViewById(R.id.groupNameEditText);
        EditText groupSymbolEditText = alertDialogCustomView.findViewById(R.id.groupSymbolEditText);

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
                Toast.makeText(GroupActivity.this, groupNameEditText.getText().toString()+" "+
                        groupSymbolEditText.getText().toString(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Faculty item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(GroupActivity.this, FacultyActivity.class);
                startActivity(intent);
                return true;
            case R.id.chair:
                Toast.makeText(this, "Chair item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(GroupActivity.this, ChairActivity.class);
                startActivity(intent);
                return true;
            case R.id.group:
                Toast.makeText(this, "Group item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(GroupActivity.this, GroupActivity.class);
                startActivity(intent);
                return true;
            case R.id.timetable:
                Toast.makeText(this, "Timetable item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(GroupActivity.this, TimetableActivity.class);
                startActivity(intent);
                return true;
            case R.id.users:
                Toast.makeText(this, "Users item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(GroupActivity.this, UsersActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout:
                Toast.makeText(this, "Logout item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(GroupActivity.this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}