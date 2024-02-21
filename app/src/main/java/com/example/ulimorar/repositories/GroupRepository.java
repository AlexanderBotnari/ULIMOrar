package com.example.ulimorar.repositories;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.ulimorar.R;
import com.example.ulimorar.activities.GroupActivity;
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

import java.util.ArrayList;
import java.util.List;

public class GroupRepository {

    private DatabaseReference facultiesDatabaseReference;

    private MutableLiveData<List<Group>> groupListLiveData = new MutableLiveData<>();

    private List<Group> groupList;

    public GroupRepository(){
        facultiesDatabaseReference = FirebaseDatabase.getInstance().getReference().
                child("faculties");

    }

    public void getGroups(String currentChairKey, Faculty currentFaculty){
        if (currentChairKey != null){
            Query query = FirebaseDatabase.getInstance().getReference("faculties").child(currentFaculty.getId())
                    .child("chairs").child(String.valueOf(currentChairKey)).child("groups");
            query.addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    groupList = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot groupSnapshot : snapshot.getChildren()) {
                            Group group = groupSnapshot.getValue(Group.class);
                            groupList.add(group);
                        }
                        groupListLiveData.postValue(groupList);
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        groupListLiveData.postValue(null);
                }
            });
        }
    }

    public void addNewGroupToChair(Chair currentChair, Faculty currentFaculty, String currentChairKey,
                                   String groupName, String groupSymbol, Activity activity, AlertDialog alertDialog) {
        Group group = new Group(groupName, groupSymbol);
        groupList.add(group);

        currentChair.setGroups(groupList);

        if (currentChairKey != null){

            facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs")
                    .child(currentChairKey).setValue(currentChair).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    }

    public void editGroup(Group groupToUpdate, Faculty currentFaculty, String currentChairKey,
                          int groupPositionToUpdate, String groupName, String groupSymbol,
                          Activity activity, AlertDialog alertDialog){
        Group newGroup = new Group(groupName, groupSymbol);
        newGroup.setTimetables(groupToUpdate.getTimetables());

        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs")
                .child(currentChairKey).child("groups").
                child(String.valueOf(groupPositionToUpdate)).setValue(newGroup).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    public void deleteGroup(Faculty currentFaculty, String currentChairKey, int groupPositionToDelete,
                            Activity activity, DeleteBottomSheetFragment bottomSheetFragment){
        facultiesDatabaseReference.child(currentFaculty.getId()).child("chairs")
                .child(currentChairKey).child("groups")
                .child(String.valueOf(groupPositionToDelete)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
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

    public MutableLiveData<List<Group>> getGroupListLiveData() {
        return groupListLiveData;
    }
}
