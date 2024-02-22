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
import com.example.ulimorar.activities.GroupActivity;
import com.example.ulimorar.activities.TimetableActivity;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimetableRepository {

    private DatabaseReference facultiesDatabaseReference;

    private StorageReference storageReference;

    private MutableLiveData<List<Timetable>> timetableListLiveData = new MutableLiveData<>();

    private List<Timetable> timetableList;

    public TimetableRepository() {
        facultiesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("faculties");
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public void getTimetables(Faculty currentFaculty, String chairIndex, String groupIndex) {
        Query query = facultiesDatabaseReference.child(currentFaculty.getId())
                .child("chairs").child(chairIndex).child("groups").child(groupIndex)
                .child("timetables");
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                timetableList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot timetableSnapshot : snapshot.getChildren()) {
                        Timetable timetable = timetableSnapshot.getValue(Timetable.class);
                        timetableList.add(timetable);
                    }
                    timetableListLiveData.postValue(timetableList);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                    timetableListLiveData.postValue(null);
            }
        });
    }

    public void addNewTimetableToGroup(Faculty currentFaculty, Group currentGroup, String timetableName,
                                       String chairIndex, String groupIndex, Activity activity,
                                       AlertDialog alertDialog, Uri selectedImageUri) {
        Timetable timetable = new Timetable(timetableName, new Date().getTime());

        timetableList.add(timetable);
        currentGroup.setTimetables(timetableList);

        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs").
                child(chairIndex).child("groups").child(groupIndex).setValue(currentGroup).addOnCompleteListener(new OnCompleteListener<Void>() {
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

        // Upload the selected image to Firebase Storage or perform other actions
        uploadImageToFirebaseStorage(selectedImageUri, timetable.getTimetableName(), timetable.getUpdateTime(),
                String.valueOf(timetableList.indexOf(timetable)), currentGroup, activity, currentFaculty,
                chairIndex, groupIndex);
    }

    private void uploadImageToFirebaseStorage(Uri imageUri, String sessionName, Long date, String timetableIndex,
                                              Group currentGroup, Activity activity, Faculty currentFaculty,
                                              String chairIndex, String groupIndex) {
        if (imageUri != null) {
            // Create a reference to "images/[filename]"
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
                                            child(chairIndex).child("groups").child(groupIndex).
                                            child("timetables").child(timetableIndex).
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

    public void editTimetable(Integer itemPosition, String timetableName, Uri selectedImageUri,
                              Timetable timetableToUpdate, Group currentGroup, Faculty currentFaculty,
                              String chairIndex, String groupIndex, Activity activity, AlertDialog alertDialog){
        Timetable newTimetable = new Timetable(timetableName,  new Date().getTime());

        if (selectedImageUri != null){
            storageReference.child("timetables/" + currentGroup.getGroupName() + "-" +
                    timetableToUpdate.getTimetableName() + "-" + timetableToUpdate.getUpdateTime() + ".jpg").delete();
            uploadImageToFirebaseStorage(selectedImageUri, newTimetable.getTimetableName(), newTimetable.getUpdateTime(),
                    String.valueOf(itemPosition), currentGroup, activity, currentFaculty,
                    chairIndex, groupIndex);
        }else{
            newTimetable.setImageUrl(timetableToUpdate.getImageUrl());
        }

        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs").
                child(chairIndex).child("groups").child(groupIndex).
                child("timetables").child(String.valueOf(itemPosition))
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

    public void deleteTimetable(String timetablePosition ,Timetable timetableToDelete, Faculty currentFaculty,
                                String chairIndex, String groupIndex, Group currentGroup, Activity activity){

        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs").
                child(chairIndex).child("groups").child(groupIndex).
                child("timetables").child(timetablePosition).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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

    public MutableLiveData<List<Timetable>> getTimetableListLiveData() {
        return timetableListLiveData;
    }
}
