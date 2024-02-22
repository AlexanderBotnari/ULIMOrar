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

public class TimetableViewModel extends ViewModel {

    private TimetableRepository timetableRepository;
    private MutableLiveData<List<Timetable>> timetableListLiveData;

    public TimetableViewModel(){
        timetableRepository = new TimetableRepository();
        timetableListLiveData = timetableRepository.getTimetableListLiveData();
    }

    public void getTimetables(Faculty currentFaculty, String chairIndex, String groupIndex) {
        timetableRepository.getTimetables(currentFaculty, chairIndex, groupIndex);
    }

    public void addNewTimetableToGroup(Faculty currentFaculty, Group currentGroup, String timetableName,
                                       String chairIndex, String groupIndex, Activity activity,
                                       AlertDialog alertDialog, Uri selectedImageUri) {
        timetableRepository.addNewTimetableToGroup(currentFaculty, currentGroup, timetableName, chairIndex,
                groupIndex, activity, alertDialog, selectedImageUri);
    }

    public void editTimetable(Integer itemPosition, String timetableName, Uri selectedImageUri,
                              Timetable timetableToUpdate, Group currentGroup, Faculty currentFaculty,
                              String chairIndex, String groupIndex, Activity activity, AlertDialog alertDialog){
        timetableRepository.editTimetable(itemPosition, timetableName, selectedImageUri, timetableToUpdate,
                currentGroup, currentFaculty, chairIndex, groupIndex, activity, alertDialog);
    }

    public void deleteTimetable(String timetablePosition ,Timetable timetableToDelete, Faculty currentFaculty,
                                String chairIndex, String groupIndex, Group currentGroup, Activity activity){
        timetableRepository.deleteTimetable(timetablePosition, timetableToDelete, currentFaculty, chairIndex,
                groupIndex, currentGroup, activity);
    }

    public MutableLiveData<List<Timetable>> getTimetableListLiveData() {
        return timetableListLiveData;
    }
}
