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
import com.example.ulimorar.entities.Group;
import com.example.ulimorar.entities.Timetable;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimetableRepository {

    private DatabaseReference facultiesDatabaseReference;

    private StorageReference storageReference;

    private MutableLiveData<Map<String, Timetable>> timetableListLiveData = new MutableLiveData<Map<String, Timetable>>();

    private Map<String, Timetable> timetableMap;

    public TimetableRepository() {
        facultiesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("faculties");
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public void getTimetables(Faculty currentFaculty, String chairId, String groupId) {
        Query query = facultiesDatabaseReference.child(currentFaculty.getId())
                .child("chairs").child(chairId).child("groups").child(groupId)
                .child("timetables");
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                timetableMap = new HashMap<>();
                if (snapshot.exists()) {
                    for (DataSnapshot timetableSnapshot : snapshot.getChildren()) {
                        Timetable timetable = timetableSnapshot.getValue(Timetable.class);
                        timetableMap.put(timetableSnapshot.getKey(), timetable);
                    }
                    timetableListLiveData.postValue(timetableMap);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    timetableListLiveData.postValue(null);
            }
        });
    }

    public void addNewTimetableToGroup(Faculty currentFaculty, Group currentGroup, String timetableName,
                                       String chairId, Activity activity,
                                       AlertDialog alertDialog, Uri selectedImageUri) {
        DatabaseReference databaseReference = facultiesDatabaseReference.child(currentFaculty.getId())
                .child("chairs").child(chairId).child("groups").child(currentGroup.getId())
                .child("timetables");

        String timetableId = databaseReference.push().getKey();

        Timetable timetable = new Timetable(timetableId, timetableName, new Date().getTime());

        timetableMap.put(timetableId, timetable);

        currentGroup.setTimetables(timetableMap);

        databaseReference.child(timetableId).setValue(timetable).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                Toast.makeText(activity, R.string.add_timetable_successful_message, Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(activity, R.string.failure_add_timetable_error, Toast.LENGTH_SHORT).show();
                Log.d("FailureAddTimetable", e.getMessage());
            }
        });

        // Upload the selected image to Firebase Storage
        uploadImageToFirebaseStorage(selectedImageUri, timetable.getTimetableName(), timetable.getUpdateTime(),
                timetableId, currentGroup, activity, currentFaculty,
                chairId);
    }

    private void uploadImageToFirebaseStorage(Uri imageUri, String sessionName, Long date, String timetableId,
                                              Group currentGroup, Activity activity, Faculty currentFaculty,
                                              String chairId) {
        if (imageUri != null) {
            // Create a reference to "timetables/[filename]"
            StorageReference imageFacultyRef = storageReference.child("timetables/" + currentGroup.getGroupName() + "-" + sessionName + "-" + date + ".jpg");

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
                                    facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs").
                                            child(chairId).child("groups").child(currentGroup.getId()).
                                            child("timetables").child(timetableId).
                                            child("imageUrl").setValue(downloadUrl.toString());
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

    public void editTimetable(String timetableName, Uri selectedImageUri,
                              Timetable timetableToUpdate, Group currentGroup, Faculty currentFaculty,
                              String chairId, Activity activity, AlertDialog alertDialog){
        Timetable newTimetable = new Timetable(timetableToUpdate.getId(), timetableName,  new Date().getTime());

        if (selectedImageUri != null){
            storageReference.child("timetables/" + currentGroup.getGroupName() + "-" +
                    timetableToUpdate.getTimetableName() + "-" + timetableToUpdate.getUpdateTime() + ".jpg").delete();
            uploadImageToFirebaseStorage(selectedImageUri, newTimetable.getTimetableName(), newTimetable.getUpdateTime(),
                    timetableToUpdate.getId(), currentGroup, activity, currentFaculty,
                    chairId);
        }else{
            newTimetable.setImageUrl(timetableToUpdate.getImageUrl());
        }

        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs").
                child(chairId).child("groups").child(currentGroup.getId()).
                child("timetables").child(timetableToUpdate.getId())
                .setValue(newTimetable).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(activity, R.string.update_timetable_success, Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(activity, R.string.update_timetable_failure, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteTimetable(Timetable timetableToDelete, Faculty currentFaculty,
                                String chairId, Group currentGroup, Activity activity){

        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs").
                child(chairId).child("groups").child(currentGroup.getId()).
                child("timetables").child(timetableToDelete.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                storageReference.child("timetables/" + currentGroup.getGroupName() + "-" +
                        timetableToDelete.getTimetableName() + "-" + timetableToDelete.getUpdateTime() + ".jpg").delete();
                Toast.makeText(activity, R.string.delete_timetable_success, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(activity, R.string.delete_timetable_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public MutableLiveData<Map<String, Timetable>> getTimetableListLiveData() {
        return timetableListLiveData;
    }
}
