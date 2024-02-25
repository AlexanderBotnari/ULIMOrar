package com.example.ulimorar.viewmodels;

import android.app.Activity;
import android.app.AlertDialog;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ulimorar.entities.Chair;
import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.fragments.DeleteBottomSheetFragment;
import com.example.ulimorar.repositories.ChairRepository;

import java.util.HashMap;
import java.util.Map;

public class ChairViewModel extends ViewModel {

    private ChairRepository chairRepository;
    private MutableLiveData<Map<String, Chair>> chairsListLiveData;

    public ChairViewModel(){
        chairRepository = new ChairRepository();
        chairsListLiveData = chairRepository.getChairsListLiveData();
    }

    public void getChairs(Faculty currentFaculty){
        chairRepository.getChairs(currentFaculty);
    }

    public void addNewChairToFaculty(Faculty currentFaculty, String chairName,
                                     Activity activity, AlertDialog alertDialog){
        chairRepository.addNewChairToFaculty(currentFaculty, chairName, activity, alertDialog);
    }

    public void editChair(Faculty currentFaculty, Chair chairToUpdate,
                          String chairName, Activity activity, AlertDialog alertDialog){
        chairRepository.editChair(currentFaculty, chairToUpdate, chairName, activity, alertDialog);
    }

    public void deleteChair(Faculty currentFaculty, String chairIdToDelete, Activity activity,
                            DeleteBottomSheetFragment bottomSheetFragment){
        chairRepository.deleteChair(currentFaculty, chairIdToDelete, activity, bottomSheetFragment);
    }

    public MutableLiveData<Map<String, Chair>> getChairsListLiveData() {
        return chairsListLiveData;
    }
}
