package com.example.ulimorar.repositories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.ulimorar.R;
import com.example.ulimorar.entities.Faculty;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FacultyRepository {

    private DatabaseReference facultyDatabaseReference;
    private StorageReference storageReference;

    private MutableLiveData<List<Faculty>> facultyListLiveData = new MutableLiveData<>();

    public FacultyRepository() {
        facultyDatabaseReference = FirebaseDatabase.getInstance().getReference("faculties");
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public void getFaculties(){
        Query query = FirebaseDatabase.getInstance().getReference("faculties");
        List<Faculty> faculties = new ArrayList<>();
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                faculties.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot facultySnapshot : snapshot.getChildren()) {
                        Faculty faculty = facultySnapshot.getValue(Faculty.class);
                        faculties.add(faculty);
                    }
                    facultyListLiveData.postValue(faculties);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    facultyListLiveData.postValue(null);
            }
        });
    }

    public void addFaculty(Activity activity, String facultyName, String facultyDescription, Uri imageUri, AlertDialog alertDialog){
        String facultyId = facultyDatabaseReference.push().getKey();

        Faculty faculty = new Faculty(facultyId, facultyName, facultyDescription);

        // Add the faculty to the database
        assert facultyId != null;
        facultyDatabaseReference.child(facultyId).setValue(faculty).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(activity, R.string.add_faculty_successful_message, Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            } else {
                Toast.makeText(activity, R.string.add_faculty_failure_message, Toast.LENGTH_SHORT).show();
                Log.d("FailureAddFaculty", task.getException().getMessage());
            }
        });

        // Upload the selected image to Firebase Storage
        uploadImageToFirebaseStorage(imageUri, facultyId, activity);
    }

    public void editFaculty(String facultyId, String facultyName, String facultyDescription,
                            Faculty facultyToUpdate, Uri imageUri, Activity activity, AlertDialog alertDialog){

        Faculty faculty = new Faculty(facultyId, facultyName, facultyDescription);
        faculty.setChairs(facultyToUpdate.getChairs());

        if (imageUri != null){
            storageReference.child("faculties/" + facultyId + ".jpg").delete();
            uploadImageToFirebaseStorage(imageUri, facultyId, activity);
        }else{
            faculty.setFacultyPosterPath(facultyToUpdate.getFacultyPosterPath());
        }

        facultyDatabaseReference.child(facultyId).setValue(faculty).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(activity, R.string.update_faculty_success, Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            } else {
                Toast.makeText(activity, R.string.update_faculty_failure, Toast.LENGTH_SHORT).show();
                Log.d("FailureAddFaculty", task.getException().getMessage());
            }
        });
    }

    public void deleteFaculty(Faculty facultyToDelete, Activity activity){
        facultyDatabaseReference.child(facultyToDelete.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                storageReference.child("faculties/" + facultyToDelete.getId() + ".jpg").delete();
                Toast.makeText(activity, R.string.delete_faculty_success, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(activity, R.string.delete_faculty_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void uploadImageToFirebaseStorage(Uri imageUri, String facultyId, Activity activity) {
        if (imageUri != null) {
            // Create a reference to "faculties/[filename]"
            StorageReference imageFacultyRef = storageReference.child("faculties/" + facultyId + ".jpg");

            // Upload the file to Firebase Storage
            imageFacultyRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image uploaded successfully
                            Toast.makeText(activity, R.string.image_uploaded_successful_message, Toast.LENGTH_SHORT).show();

                            // Get the download URL and update the faculty by id in realtime database
                            imageFacultyRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUrl) {
                                    Log.d("DownloadUrl", downloadUrl.toString());
                                    facultyDatabaseReference.child(facultyId).child("facultyPosterPath").setValue(downloadUrl.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            // Handle unsuccessful uploads
                            Toast.makeText(activity, R.string.failure_image_upload, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public MutableLiveData<List<Faculty>> getFacultyListLiveData() {
        return facultyListLiveData;
    }
}
