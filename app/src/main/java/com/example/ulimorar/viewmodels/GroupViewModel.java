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
import java.util.Map;

public class GroupViewModel extends ViewModel {

    private GroupRepository groupRepository;

    private MutableLiveData<Map<String, Group>> groupListLiveData;

    public GroupViewModel(){
        groupRepository = new GroupRepository();
        groupListLiveData = groupRepository.getGroupListLiveData();
    }

    public void getGroups(String currentChairId, Faculty currentFaculty){
        groupRepository.getGroups(currentChairId, currentFaculty);
    }

    public void addNewGroupToChair(Chair currentChair, Faculty currentFaculty,
                                   String groupName, String groupSymbol, Activity activity, AlertDialog alertDialog) {
        groupRepository.addNewGroupToChair(currentChair, currentFaculty, groupName,
                groupSymbol, activity, alertDialog);
    }

    public void editGroup(Group groupToUpdate, Faculty currentFaculty, String currentChairId,
                          String groupName, String groupSymbol,
                          Activity activity, AlertDialog alertDialog){
        groupRepository.editGroup(groupToUpdate, currentFaculty, currentChairId,
                groupName, groupSymbol, activity, alertDialog);
    }

    public void deleteGroup(Faculty currentFaculty, String currentChairId, String groupIdToDelete,
                            Activity activity, DeleteBottomSheetFragment bottomSheetFragment){
        groupRepository.deleteGroup(currentFaculty, currentChairId, groupIdToDelete, activity, bottomSheetFragment);
    }

    public MutableLiveData<Map<String, Group>> getGroupListLiveData() {
        return groupListLiveData;
    }
}
