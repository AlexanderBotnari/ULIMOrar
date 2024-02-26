package com.example.ulimorar.viewmodels;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ulimorar.entities.Faculty;
import com.example.ulimorar.entities.Group;
import com.example.ulimorar.entities.Timetable;
import com.example.ulimorar.repositories.TimetableRepository;

import java.util.List;
import java.util.Map;

public class TimetableViewModel extends ViewModel {

    private TimetableRepository timetableRepository;
    private MutableLiveData<Map<String, Timetable>> timetableListLiveData;

    public TimetableViewModel(){
        timetableRepository = new TimetableRepository();
        timetableListLiveData = timetableRepository.getTimetableListLiveData();
    }

    public void getTimetables(Faculty currentFaculty, String chairId, String groupId) {
        timetableRepository.getTimetables(currentFaculty, chairId, groupId);
    }

    public void addNewTimetableToGroup(Faculty currentFaculty, Group currentGroup, String timetableName,
                                       String chairId, Activity activity,
                                       AlertDialog alertDialog, Uri selectedImageUri) {
        timetableRepository.addNewTimetableToGroup(currentFaculty, currentGroup, timetableName, chairId,
                 activity, alertDialog, selectedImageUri);
    }

    public void editTimetable(String timetableName, Uri selectedImageUri,
                              Timetable timetableToUpdate, Group currentGroup, Faculty currentFaculty,
                              String chairId, Activity activity, AlertDialog alertDialog){
        timetableRepository.editTimetable(timetableName, selectedImageUri, timetableToUpdate,
                currentGroup, currentFaculty, chairId, activity, alertDialog);
    }

    public void deleteTimetable(Timetable timetableToDelete, Faculty currentFaculty,
                                String chairId, Group currentGroup, Activity activity){
        timetableRepository.deleteTimetable(timetableToDelete, currentFaculty, chairId,
                currentGroup, activity);
    }

    public MutableLiveData<Map<String, Timetable>> getTimetableListLiveData() {
        return timetableListLiveData;
    }
}
