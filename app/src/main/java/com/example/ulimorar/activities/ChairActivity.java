package com.example.ulimorar.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;

public class ChairActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.chairs_activity_title);
        setContentView(R.layout.activity_chair);
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
                intent = new Intent(ChairActivity.this, FacultyActivity.class);
                startActivity(intent);
                return true;
            case R.id.chair:
                Toast.makeText(this, "Chair item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(ChairActivity.this, ChairActivity.class);
                startActivity(intent);
                return true;
            case R.id.group:
                Toast.makeText(this, "Group item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(ChairActivity.this, GroupActivity.class);
                startActivity(intent);
                return true;
            case R.id.timetable:
                Toast.makeText(this, "Timetable item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(ChairActivity.this, TimetableActivity.class);
                startActivity(intent);
                return true;
            case R.id.users:
                Toast.makeText(this, "Users item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(ChairActivity.this, UsersActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout:
                Toast.makeText(this, "Logout item clicked!", Toast.LENGTH_SHORT).show();
                intent = new Intent(ChairActivity.this, LoginActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}