package com.example.ulimorar.viewmodels;

import android.app.Activity;
import android.app.AlertDialog;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ulimorar.entities.Chair;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.entities.Group;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.repositories.GroupRepository;

import java.util.List;

public class GroupViewModel extends ViewModel {

    private GroupRepository groupRepository;

    private MutableLiveData<List<Group>> groupListLiveData;

    public GroupViewModel(){
        groupRepository = new GroupRepository();
        groupListLiveData = groupRepository.getGroupListLiveData();
    }

    public void getGroups(String currentChairKey, Faculty currentFaculty){
        groupRepository.getGroups(currentChairKey, currentFaculty);
    }

    public void addNewGroupToChair(Chair currentChair, Faculty currentFaculty, String currentChairKey,
                                   String groupName, String groupSymbol, Activity activity, AlertDialog alertDialog) {
        groupRepository.addNewGroupToChair(currentChair, currentFaculty, currentChairKey, groupName,
                groupSymbol, activity, alertDialog);
    }

    public void editGroup(Group groupToUpdate, Faculty currentFaculty, String currentChairKey,
                          int groupPositionToUpdate, String groupName, String groupSymbol,
                          Activity activity, AlertDialog alertDialog){
        groupRepository.editGroup(groupToUpdate, currentFaculty, currentChairKey,
                groupPositionToUpdate, groupName, groupSymbol, activity, alertDialog);
    }

    public void deleteGroup(Faculty currentFaculty, String currentChairKey, int groupPositionToDelete,
                            Activity activity, DeleteBottomSheetFragment bottomSheetFragment){
        groupRepository.deleteGroup(currentFaculty, currentChairKey, groupPositionToDelete, activity, bottomSheetFragment);
    }

    public MutableLiveData<List<Group>> getGroupListLiveData() {
        return groupListLiveData;
    }
}
