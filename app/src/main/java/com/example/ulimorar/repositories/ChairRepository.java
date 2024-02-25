package com.example.ulimorar.repositories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.ulimorar.R;
import com.example.ulimorar.entities.Chair;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChairRepository {

    private DatabaseReference facultiesDatabaseReference;

    private MutableLiveData<Map<String, Chair>> chairsListLiveData = new MutableLiveData<Map<String, Chair>>();

    private List<Chair> chairsList;

    public ChairRepository() {
        facultiesDatabaseReference = FirebaseDatabase.getInstance().getReference("faculties");
    }

    public void getChairs(Faculty currentFaculty) {
        Query query = facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs");
        Map<String, Chair> chairs = new HashMap<>();
        query.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                chairs.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot chairSnapshot : snapshot.getChildren()) {
                        Chair chair = chairSnapshot.getValue(Chair.class);
                        chairs.put(chairSnapshot.getKey(), chair);
                    }
                    chairsListLiveData.postValue(chairs);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                chairsListLiveData.postValue(null);
            }
        });
    }


    public void addNewChairToFaculty(Faculty currentFaculty, String chairName, Activity activity, AlertDialog alertDialog) {
        DatabaseReference chairsReference = facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs");

        String chairId = chairsReference.push().getKey();

        if (currentFaculty.getChairs() == null) {
            currentFaculty.setChairs(new HashMap<>());
        }

        Chair chair = new Chair(chairId, chairName, String.valueOf(chairName.charAt(0)));

        currentFaculty.getChairs().put(chairId, chair);

        chairsReference.child(chairId).setValue(chair).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(activity, R.string.add_chair_successful_message, Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(activity, R.string.failure_add_chair_error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public void editChair(Faculty currentFaculty, Chair chairToUpdate,
                          String chairName, Activity activity, AlertDialog alertDialog){
        Chair newChair = new Chair(chairToUpdate.getId(), chairName, String.valueOf(chairName.charAt(0)));
        newChair.setGroups(chairToUpdate.getGroups());

        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs").
                child(chairToUpdate.getId()).setValue(newChair).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(activity, R.string.update_chair_success, Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(activity, R.string.update_chair_failure, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteChair(Faculty currentFaculty, String chairIdToDelete, Activity activity,
                            DeleteBottomSheetFragment bottomSheetFragment){
        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs").
                child(chairIdToDelete).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(activity, R.string.delete_chair_success, Toast.LENGTH_SHORT).show();
                        bottomSheetFragment.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(activity, R.string.delete_chair_failure, Toast.LENGTH_SHORT).show();
                        bottomSheetFragment.dismiss();
                    }
                });
    }


    public MutableLiveData<Map<String, Chair>> getChairsListLiveData() {
        return chairsListLiveData;
    }


}
