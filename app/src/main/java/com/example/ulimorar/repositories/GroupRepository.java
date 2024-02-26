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
import com.example.ulimorar.entities.Group;
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

import java.util.HashMap;
import java.util.Map;

public class GroupRepository {

    private DatabaseReference facultiesDatabaseReference;

    private MutableLiveData<Map<String, Group>> groupListLiveData = new MutableLiveData<Map<String, Group>>();

    private Map<String, Group> groupMap;

    public GroupRepository(){
        facultiesDatabaseReference = FirebaseDatabase.getInstance().getReference().
                child("faculties");
    }

    public void getGroups(String currentChairId, Faculty currentFaculty){
        if (currentChairId != null){
            Query query = FirebaseDatabase.getInstance().getReference("faculties").child(currentFaculty.getId())
                    .child("chairs").child(currentChairId).child("groups");

            query.addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    groupMap = new HashMap<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                            Group group = groupSnapshot.getValue(Group.class);
                            groupMap.put(groupSnapshot.getKey(), group);
                        }
                        groupListLiveData.postValue(groupMap);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        groupListLiveData.postValue(null);
                }
            });
        }
    }

    public void addNewGroupToChair(Chair currentChair, Faculty currentFaculty, String groupName,
                                   String groupSymbol, Activity activity, AlertDialog alertDialog) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("faculties")
                .child(currentFaculty.getId()).child("chairs").child(currentChair.getId())
                .child("groups");

        String groupId = databaseReference.push().getKey();

        Group group = new Group(groupId, groupName, groupSymbol);

        groupMap.put(groupId, group);

        currentChair.setGroups(groupMap);

            databaseReference.child(groupId).setValue(group).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    Toast.makeText(activity, R.string.add_group_successful_message, Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Toast.makeText(activity, R.string.failure_add_group_error, Toast.LENGTH_SHORT).show();
                }
            });
    }

    public void editGroup(Group groupToUpdate, Faculty currentFaculty, String currentChairKey,
                          String groupName, String groupSymbol,
                          Activity activity, AlertDialog alertDialog){
        Group newGroup = new Group(groupToUpdate.getId(), groupName, groupSymbol);
        newGroup.setTimetables(groupToUpdate.getTimetables());

        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs")
                .child(currentChairKey).child("groups").
                child(groupToUpdate.getId()).setValue(newGroup).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(activity, R.string.update_group_success, Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(activity, R.string.update_group_success, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteGroup(Faculty currentFaculty, String currentChairId, String groupIdToDelete,
                            Activity activity, DeleteBottomSheetFragment bottomSheetFragment){
        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs")
                .child(currentChairId).child("groups")
                .child(groupIdToDelete).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        Toast.makeText(activity, R.string.delete_group_success, Toast.LENGTH_SHORT).show();
                        bottomSheetFragment.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(activity, R.string.delete_group_failure, Toast.LENGTH_SHORT).show();
                        bottomSheetFragment.dismiss();
                    }
                });
    }

    public MutableLiveData<Map<String, Group>> getGroupListLiveData() {
        return groupListLiveData;
    }
}
