package com.example.ulimorar.activities;

import android.content.Intent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ortiz.touchview.TouchImageView;
import com.squareup.picasso.Picasso;

public class TimetableFullscreenActivity extends AppCompatActivity {

    private TouchImageView photoImageView;
    private FloatingActionButton closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppCompat_NoActionBar);
        setContentView(R.layout.activity_timetable_fullscreen);

        Intent intent = getIntent();
        String photoUrl = intent.getStringExtra("photoUrl");

        photoImageView = findViewById(R.id.timetableImageView);
        closeButton = findViewById(R.id.closeFloatingButton);

        Picasso.get().load(photoUrl).into(photoImageView);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}